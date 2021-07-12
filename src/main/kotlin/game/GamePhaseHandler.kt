package game

import dI
import display.event.KeyboardEvent
import display.event.MouseButtonEvent
import display.event.MouseScrollEvent
import display.graphic.Color
import display.gui.element.GuiLabel
import display.text.TextJustify
import engine.freeBody.Vehicle
import engine.freeBody.Warhead
import engine.gameState.GameStateSimulator.getNewPrediction
import engine.motion.Director
import game.GamePhase.*
import game.shield.Diamagnetor
import game.shield.Refractor
import input.CameraView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.jbox2d.common.Vec2
import org.lwjgl.glfw.GLFW
import utility.Common.getRandomDirection
import utility.Common.getTimingFunctionEaseIn
import utility.Common.getTimingFunctionEaseOut
import utility.Common.makeVec2Circle
import utility.Common.pressAndHoldAction
import utility.Common.vectorUnit
import utility.StopWatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.math.ceil
import kotlin.math.roundToInt

class GamePhaseHandler {

    private val timeStep = 1f / 60f
    private val velocityIterations = 8
    private val positionIterations = 3

    private val gameState = dI.gameState
    val drawer = dI.drawer
    private val camera = dI.cameraView
    private val guiController = dI.guiController

    private var currentPhase = NONE
    private var isTransitioning = false

    private val stopWatch = StopWatch()

    var latestPrediction = TrajectoryPrediction(stopWatch.currentTime)
    var playerAimChanged = PublishSubject.create<Boolean>()
    private val unsubscribe = PublishSubject.create<Boolean>()

    private val phaseUpdateMap: HashMap<GamePhase, () -> Unit>
    private val phaseRenderMap: HashMap<GamePhase, () -> Unit>

    val damagedVehicles
        get() = lastVehicleDamageRecords
            .filter { (vehicle, oldHp) -> vehicle.hitPoints < oldHp }
            .map { (vehicle, _) -> vehicle }
            .toList()

    private var lastVehicleDamageRecords = HashMap<Vehicle, Float>()
    private var lastVehicleWatched: Vehicle? = null
    private var playerLabels: MutableMap<Vehicle, GuiLabel>? = null

    init {
        phaseUpdateMap = HashMap(mutableMapOf(
            Pair(PAUSE, { if (isTransitioning) tickGamePausing() }),
            Pair(PLAY, { if (isTransitioning) tickGameUnpausing() }),
            Pair(MAIN_MENU, { }),
            Pair(MAIN_MENU_SELECT_PLAYERS, { }),
            Pair(NEW_GAME_INTRO, { handleIntro() }),
            Pair(PLAYERS_PICK_SHIELDS, { guiController.update() }),
            Pair(PLAYERS_TURN, { guiController.update() }),
            Pair(PLAYERS_TURN_FIRED, { handlePlayerFired() }),
            Pair(PLAYERS_TURN_FIRED_WAIT_STABILIZE, { waitOnPhase(stabilizeTimeDuration, PLAYERS_TURN_ENDING) }),
            Pair(PLAYERS_TURN_ENDING, { handlePlayerTurnEnding() }),
            Pair(PLAYERS_TURN_END, { handlePlayerTurnEnd() }),
            Pair(PLAYERS_TURN_JUMPED, { handlePlayerJumped() }),
            Pair(PLAYERS_TURN_AIMING, { guiController.update() }),
            Pair(PLAYERS_WATCH_DAMAGE_DEALT_INTRO, {
                guiController.update()
                waitOnPhase(cameraPanDuration, PLAYERS_WATCH_DAMAGE_DEALT, true)
            }),
            Pair(PLAYERS_WATCH_DAMAGE_DEALT, {
                guiController.update()
                handlePlayerWatchDamageDealt()
            }),
            Pair(END_ROUND, { handleEndRound() })
        ))

        phaseRenderMap = HashMap(mutableMapOf(
            Pair(MAIN_MENU, { guiController.render() }),
            Pair(MAIN_MENU_SELECT_PLAYERS, { guiController.render() }),
            Pair(PLAYERS_PICK_SHIELDS, { drawWorldAndGui() }),
            Pair(PLAYERS_TURN, {
                drawWorldAndGui()
                drawPlayerAimingGui()
            }),
            Pair(PLAYERS_TURN_AIMING, {
                drawWorldAndGui()
                drawPlayerAimingGui()
            }),
            Pair(PLAYERS_TURN_JUMPED, { drawWorldAndGui() }),
            Pair(PLAYERS_WATCH_DAMAGE_DEALT_INTRO, { drawWorldAndGui() }),
            Pair(PLAYERS_WATCH_DAMAGE_DEALT, { drawWorldAndGui() }),
            Pair(END_ROUND, { drawWorldAndGui() })
        ))
    }

    fun init() {
        when (2) {
            0 -> setupMainMenu()
            1 -> setupMainMenuSelectPlayers()
            2 -> {
                currentPhase = PLAYERS_PICK_SHIELDS
                isTransitioning = false
                gameState.reset()
                gameState.gamePlayers.addAll((1..2).map { GamePlayer("Player $it", cash = 1000f) })
                MapGenerator.populateNewGameMap(gameState)
                gameState.planets.toList().filter { it.worldBody.mass == it.worldBody.mass.coerceIn(140f, 160f) }
                    .forEach {
                        gameState.world.destroyBody(it.worldBody)
                        gameState.planets.remove(it)
                    }

                val planet = gameState.gravityBodies.maxByOrNull { it.worldBody.mass }!!
                gameState.vehicles.forEach {
                    it.worldBody.setTransform(planet.worldBody.position
                        .add(makeVec2Circle(getRandomDirection()).mul(planet.radius + 23f)),
                        0f)
                }

                gameState.gamePlayers.forEach { it.vehicle!!.shield = Diamagnetor(it.vehicle!!) }
                gameState.playerOnTurn = gameState.gamePlayers.first()

                setupNextPlayersFireTurn()
            }
            else -> throw Throwable("Enter a debug step number to start game")
        }

        playerAimChanged.takeUntil(unsubscribe)
            .sample(33, TimeUnit.MILLISECONDS)
            .flatMap {
                val a = PublishSubject.create<TrajectoryPrediction>()

                thread {
                    a.onNext(getNewPrediction(15f, .75f, gameState, velocityIterations,
                        positionIterations, timeStep, latestPrediction))
                    a.onComplete()
                }
                a
            }
            .filter { latestPrediction.timeStamp < it.timeStamp }
            .subscribe { latestPrediction = it }
    }

    private fun startTransition() {
        stopWatch.reset()
        isTransitioning = true
    }

    private fun startNewPhase(newPhase: GamePhase) {
        currentPhase = newPhase
        startTransition()
    }

    fun update() {
        camera.update()
        val planet = gameState.gravityBodies.maxByOrNull { it.worldBody.mass }!!

        val phaseAction = phaseUpdateMap[currentPhase]
        when {
            phaseAction != null -> phaseAction()
            else -> tickClockNormalSpeed()
        }
    }

    fun render() {
        val phaseAction = phaseRenderMap[currentPhase]
        when {
            phaseAction != null -> phaseAction()
            else -> drawPlayPhase()
        }

        drawDebug()
    }

    private fun drawDebug() {
        val debugColor = Color.GREEN.setAlpha(.7f)
        drawer.renderer.drawText(
            "${if (isTransitioning) "Active" else "Idle  "} / Phase ${currentPhase.name}",
            Vec2(5f - camera.windowWidth * .5f, -10f + camera.windowHeight * .5f),
            vectorUnit.mul(0.1f), debugColor, TextJustify.LEFT, false)

        drawer.renderer.drawText(
            "GameTime ${gameState.tickTime.div(100f).roundToInt().div(10f)}s / PhaseTime ${stopWatch.elapsedTime.div(
                100f)
                .roundToInt().div(10f)}s",
            Vec2(5f - camera.windowWidth * .5f, -30f + camera.windowHeight * .5f),
            vectorUnit.mul(0.1f), debugColor, TextJustify.LEFT, false)
    }

    private fun drawPlayerAimingGui() {
        drawer.drawPlayerAimingPointer(gameState.playerOnTurn!!)
        drawer.drawWarheadTrajectory(latestPrediction)
        gameState.gravityBodies
            .filterNot { it is Vehicle && it.shield is Refractor}
            .forEach { drawer.drawMotionPredictors(it) }
    }

    private fun handleIntro() {
        val interpolationStep = stopWatch.elapsedTime / introDuration
        camera.setNewZoom(CameraView.defaultZoom -
                (CameraView.defaultZoom - CameraView.minZoom) * getTimingFunctionEaseOut(interpolationStep))

        when {
            isTransitioning -> tickGameUnpausing()
            stopWatch.elapsedTime > introDuration -> {
                camera.setNewZoom(CameraView.defaultZoom)
                playerSelectsShield()
            }
            stopWatch.elapsedTime > (introDuration - introTimeEnd) -> tickGamePausing(
                introTimeEnd, calculatedElapsedTime = (stopWatch.elapsedTime - introDuration + introTimeEnd))
            else -> tickClockNormalSpeed()
        }
    }

    private fun handlePlayerFired() {
        val roundEndsEarly = {
            gameState.warheads.none()
                    && gameState.particles.none()
                    && gameState.vehicles.all { it.isStable }
        }
        when {
            isTransitioning -> tickGameUnpausing(quickTimeStart)
            stopWatch.elapsedTime > maxTurnDuration || roundEndsEarly() -> startNewPhase(
                PLAYERS_TURN_FIRED_WAIT_STABILIZE)
            else -> tickClockNormalSpeed()
        }
    }

    private fun endOfJump() =
        (stopWatch.elapsedTime > maxTurnDuration && gameState.playerOnTurn!!.vehicle!!.fuel!!.amount < .1f)
                || stopWatch.elapsedTime > maxJumpDuration
                || gameState.playerOnTurn!!.vehicle!!.hasCollided

    private fun handlePlayerJumped() {
        guiController.update()
        when {
            isTransitioning -> tickGameUnpausing(jumpTimeStart)
            stopWatch.elapsedTime < jumpTimeSafeDuration -> {
                gameState.playerOnTurn!!.vehicle!!.hasCollided = false
                tickClockNormalSpeed()
            }
            endOfJump() -> startNewPhase(PLAYERS_TURN_FIRED_WAIT_STABILIZE)
            else -> tickClockNormalSpeed()
        }
    }

    private fun waitOnPhase(duration: Float,
                            nextGamePhase: GamePhase,
                            paused: Boolean = false,
                            callback: () -> Unit = {}) {
        when {
            stopWatch.elapsedTime > duration -> {
                startNewPhase(nextGamePhase)
                startTransition()
                callback()
            }
            paused -> return
            else -> tickClockNormalSpeed()
        }
    }

    private fun handlePlayerTurnEnding() {
        tickGamePausing(endPhase = PLAYERS_TURN_END, callback = {
            lastVehicleDamageRecords
                .filter { (vehicle, oldHp) -> oldHp - vehicle.hitPoints == 0f }
                .forEach { lastVehicleDamageRecords.remove(it.key) }
        })
    }

    private fun handlePlayerTurnEnd() {
        val damageHappened = lastVehicleDamageRecords.isNotEmpty()
        if (damageHappened) {
            guiController.clear()
            val damageRecordsList = lastVehicleDamageRecords.toList()
            playerLabels = guiController.addPlayerFakeLabels(
                listOf(damageRecordsList.first()), camera)

            lastVehicleWatched = damageRecordsList.first().first

            camera.trackFreeBody(lastVehicleWatched!!, false)
            startNewPhase(PLAYERS_WATCH_DAMAGE_DEALT_INTRO)
        } else {
            setupNextPlayersFireTurn()
        }
    }

    private fun handlePlayerWatchDamageDealt() {
        val watchedVehicle = lastVehicleWatched!!
        when {
            stopWatch.elapsedTime < watchHpDropDuration -> {
                val interpolationStep = 1f - stopWatch.elapsedTime / watchHpDropDuration
                val range = lastVehicleDamageRecords[watchedVehicle]!! - watchedVehicle.hitPoints
                val hp = (watchedVehicle.hitPoints +
                        getTimingFunctionEaseIn(interpolationStep) * range).coerceAtLeast(0f)
                playerLabels!![watchedVehicle]?.updateManual(
                    callback = { element: GuiLabel ->
                        element.title = "HP ${ceil(hp).toInt()}"
                        element.color = Color.createFromHsv((hp / 100f) * .3f, .5f, .7f)
                    })
            }
            stopWatch.elapsedTime < watchHpDropDuration + watchHpIdleDuration -> return
            else -> {
                lastVehicleDamageRecords.remove(watchedVehicle)
                currentPhase = PLAYERS_TURN_END

                if (watchedVehicle.hitPoints <= 0f) {
                    watchedVehicle.dispose(gameState.world, gameState.vehicles)
                    Warhead(watchedVehicle.id, gameState.warheads, gameState.world, watchedVehicle.player,
                        watchedVehicle.worldBody.position.x, watchedVehicle.worldBody.position.y, 0f,
                        0f, 0f, 0f, 0f, onCollision = { _, _ -> }, createdAt = gameState.tickTime - 1f)
                        .detonate(gameState.world, gameState.tickTime - 1f, gameState.warheads, gameState.particles,
                            gameState.vehicles, gameState.gravityBodies)
                }
            }
        }
    }

    private fun tickClockNormalSpeed() {
        gameState.tickClock(timeStep, velocityIterations, positionIterations)
    }

    private fun handleEndRound() {
        when {
            isTransitioning -> tickGameUnpausing(outroDuration, endSpeed = .1f)
            else -> gameState.tickClock(timeStep * .1f, velocityIterations, positionIterations)
        }
    }

    private fun playerSelectsShield() {
        if (gameState.gamePlayers.all { it.playerAim.selectedShield != null }) {
            gameState.gamePlayers.shuffle()
            setupNextPlayersFireTurn()
            gameState.gamePlayers.forEach { it.addShield() }
            return
        }

        setNextPlayerOnTurn()
        setupPlayersPickShields()

        currentPhase = PLAYERS_PICK_SHIELDS
        addPlayerLabels()
    }

    private fun setupNextPlayersFireTurn() {
        if (checkStateEndOfRound()) {
            return
        }
        setNextPlayerOnTurn()
        setupPlayerCommandPanel()
        playerAimChanged.onNext(true)

        startNewPhase(PLAYERS_TURN)
    }

    private fun checkStateEndOfRound(): Boolean {
        val vehiclesDestroyed = gameState.gamePlayers.count { it.vehicle!!.hitPoints > 0 } < 2
        if (vehiclesDestroyed) {
            startNewPhase(END_ROUND) // show damage dealt first
            guiController.createRoundLeaderboard(gameState.gamePlayers,
                onClickNextRound = { setupMainMenuSelectPlayers() })
            return true
        }
        return false
    }

    private fun setupPlayerCommandPanel() {
        guiController.createPlayerCommandPanel(
            player = gameState.playerOnTurn!!,
            onClickAim = { startNewPhase(PLAYERS_TURN_AIMING) },
            onChangeAim = { playerAimChanged.onNext(true) },
            onClickMove = { player -> playerJumps(player) },
            onClickFire = { player -> playerFires(player) }
        )
        addPlayerLabels()
    }

    private fun addPlayerLabels() {
        guiController.addPlayerLabels(gameState.gamePlayers
            .filter {
                it.vehicle!!.hitPoints > 0
                        && it.vehicle?.shield !is Refractor
            },
            camera)
    }

    private fun playerJumps(player: GamePlayer) {
        guiController.clear()
        recordLastVehicleHp()

        if (player.playerAim.selectedShield != null) {
            player.addShield()
        }

        player.startJump()
        startNewPhase(PLAYERS_TURN_JUMPED)
        guiController.createJumpFuelBar(player)
    }

    private fun playerFires(player: GamePlayer) {
        guiController.clear()
        // check() {} player has enough funds && in stable position to fire large warheads
        recordLastVehicleHp()

        if (player.playerAim.selectedShield != null) {
            player.addShield()
        }

        player.vehicle?.fireWarhead(gameState, player) { warhead -> camera.trackFreeBody(warhead) }

        startNewPhase(PLAYERS_TURN_FIRED)
    }

    private fun recordLastVehicleHp() {
        lastVehicleDamageRecords = HashMap(gameState.vehicles.map { Pair(it, it.hitPoints) }.toMap())
    }

    private fun setNextPlayerOnTurn() {
        checkNotNull(gameState.playerOnTurn) { "No player was on turn" }
        gameState.playerOnTurn?.vehicle?.shield?.setShieldEndTurn()

        val playerOnTurn = gameState.playerOnTurn!!
        val players = gameState.gamePlayers.filter { it.vehicle!!.hitPoints > 0 }
        gameState.playerOnTurn = players[(players.indexOf(playerOnTurn) + 1).rem(players.size)]
        gameState.playerOnTurn?.updateShield()

        camera.unsubscribeCheckCameraEvent.onNext(true)
        camera.trackFreeBody(gameState.playerOnTurn!!.vehicle!!, false)
    }

    private fun setupPlayersPickShields() {
        guiController.createPlayersPickShields(
            onClickShield = { playerSelectsShield() },
            player = gameState.playerOnTurn!!
        )
    }

    private fun drawWorldAndGui() {
        drawPlayPhase()
        guiController.render()
    }

    private fun drawPlayPhase() {
        gameState.background.render()
        drawer.drawBorder(gameState.mapBorder!!)

        val allFreeBodies = gameState.gravityBodies
        allFreeBodies
            .filter { !(it is Vehicle && it.shield is Refractor) }
            .forEach { drawer.drawTrail(it) }
        gameState.particles.forEach { drawer.drawParticle(it) }
        allFreeBodies.forEach { drawer.drawFreeBody(it) }
        //        allFreeBodies.forEach { drawDebugForces(it) }
        //        drawer.drawGravityCells(gameState.gravityMap, gameState.resolution)
    }

    private fun tickGameUnpausing(
        duration: Float = pauseTime,
        endPhase: GamePhase? = null,
        calculatedElapsedTime: Float? = null,
        endSpeed: Float = 1f,
        callback: () -> Unit = {}
    ) {
        val interpolateStep = (calculatedElapsedTime ?: stopWatch.elapsedTime.toFloat()) / duration
        if (interpolateStep >= 1f) {
            currentPhase = endPhase ?: currentPhase
            isTransitioning = false
            callback()
        } else {
            val timeFunctionStep = getTimingFunctionEaseOut(interpolateStep) * endSpeed
            gameState.tickClock(timeStep * timeFunctionStep, velocityIterations, positionIterations)
        }
    }

    private fun tickGamePausing(
        duration: Float = pauseTime,
        endPhase: GamePhase? = null,
        calculatedElapsedTime: Float? = null,
        endSpeed: Float = 0f,
        callback: () -> Unit = {}
    ) {
        val interpolateStep = (calculatedElapsedTime ?: stopWatch.elapsedTime.toFloat()) / duration
        if (interpolateStep >= 1f) {
            currentPhase = endPhase ?: currentPhase
            isTransitioning = false
            callback()
        } else {
            val timeFunctionStep = getTimingFunctionEaseIn(1f - interpolateStep) * (1f - endSpeed) + endSpeed
            gameState.tickClock(timeStep * timeFunctionStep, velocityIterations, positionIterations)
        }
    }

    fun scrollMouse(event: MouseScrollEvent) {
        val screenLocation = camera.getScreenLocation(event.location)
        if (guiController.locationIsGui(screenLocation)) {
            guiController.checkScroll(event.movement, screenLocation)
        } else {
            camera.moveZoom(event.movement.y)
            guiController.update()
        }
    }

    fun doubleLeftClick(location: Vec2) {
        val transformedLocation = camera.getWorldLocation(location)

        val clickedBody = gameState.gravityBodies
            .find { it.worldBody.position.add(transformedLocation.mul(-1f)).length() <= it.radius }
            ?: gameState.gravityBodies
                .map { Pair(it, it.worldBody.position.add(transformedLocation.mul(-1f)).length()) }
                .filter { (body, distance) -> distance <= body.radius + 5f }
                .minByOrNull { (_, distance) -> distance }?.first
            ?: return

        // TODO: if body is Vehicle->Refractor shield, cancel
        camera.trackFreeBody(clickedBody)
    }

    fun moveMouse(location: Vec2) {
        when {
            mouseElementPhases.any { currentPhase == it } -> guiController.checkHover(
                camera.getScreenLocation(location))
            currentPhase == PLAYERS_TURN_AIMING -> {
                val (playerOnTurn, transformedLocation, playerLocation) = getPlayerAndMouseLocations(location)
                val aimDirection = Director.getDirection(transformedLocation, playerLocation)
                playerOnTurn.playerAim.angle = aimDirection

                val distance = Director.getDistance(transformedLocation, playerLocation)
                playerOnTurn.playerAim.power = (distance - 1f) * 10f
                guiController.update()

                playerAimChanged.onNext(true)
            }
        }
    }

    private fun getPlayerAndMouseLocations(location: Vec2): Triple<GamePlayer, Vec2, Vec2> {
        checkNotNull(gameState.playerOnTurn) { "No player is on turn" }
        val playerOnTurn = gameState.playerOnTurn!!
        val transformedLocation = camera.getWorldLocation(location)
        val playerLocation = playerOnTurn.vehicle!!.worldBody.position
        return Triple(playerOnTurn, transformedLocation, playerLocation)
    }

    private fun setupMainMenu() {
        currentPhase = MAIN_MENU
        gameState.reset()
        guiController.createMainMenu(
            onClickNewGame = { setupMainMenuSelectPlayers() },
            onClickSettings = {},
            onClickQuit = { dI.window.exit() }
        )
    }

    private fun setupMainMenuSelectPlayers() {
        currentPhase = MAIN_MENU_SELECT_PLAYERS
        gameState.reset()
        repeat(2) { gameState.gamePlayers.add(GamePlayer((gameState.gamePlayers.size + 1).toString())) }
        guiController.createMainMenuSelectPlayers(
            onClickStart = { setupStartGame() },
            onClickCancel = { setupMainMenu() },
            onAddPlayer = { onAddPlayerButton() },
            onRemovePlayer = { onRemovePlayerButton() },
            playerList = gameState.gamePlayers
        )
    }

    private fun onAddPlayerButton() {
        gameState.gamePlayers.add(GamePlayer((gameState.gamePlayers.size + 1).toString()))
        guiController.updateMainMenuSelectPlayers(gameState.gamePlayers, { onAddPlayerButton() },
            { onRemovePlayerButton() })
    }

    private fun onRemovePlayerButton() {
        gameState.gamePlayers.remove(gameState.gamePlayers.last())
        guiController.updateMainMenuSelectPlayers(gameState.gamePlayers, { onAddPlayerButton() },
            { onRemovePlayerButton() })
    }

    private fun setupStartGame() {
        gameState.gamePlayers.withIndex()
            .filter { (_, player) -> player.name.length <= 1 }
            .forEach { (index, player) -> player.name = "Player ${index + 1}" }
        guiController.clear()
        camera.reset()
        startNewPhase(NEW_GAME_INTRO)

        MapGenerator.populateNewGameMap(gameState)

        check(gameState.gamePlayers.size > 1) { "Cannot play a game with less than 2 players" }
        gameState.playerOnTurn = gameState.gamePlayers.random()
    }

    fun eventKeyboardKey(startEvent: KeyboardEvent, event: Observable<KeyboardEvent>) {
        when {
            currentPhase == PLAYERS_TURN -> {
                when (startEvent.key) {
                    GLFW.GLFW_KEY_LEFT -> pressAndHoldAction(event, rampDuration = 1500f).subscribe {
                        gameState.playerOnTurn!!.playerAim.addAngle()
                        playerAimChanged.onNext(true)
                    }
                    GLFW.GLFW_KEY_RIGHT -> pressAndHoldAction(event, rampDuration = 1500f).subscribe {
                        gameState.playerOnTurn!!.playerAim.addAngle(-1f)
                        playerAimChanged.onNext(true)
                    }
                    GLFW.GLFW_KEY_UP -> pressAndHoldAction(event, rampDuration = 1500f).subscribe {
                        gameState.playerOnTurn!!.playerAim.addPower()
                        playerAimChanged.onNext(true)
                    }
                    GLFW.GLFW_KEY_DOWN -> pressAndHoldAction(event, rampDuration = 1500f).subscribe {
                        gameState.playerOnTurn!!.playerAim.addPower(-1f)
                        playerAimChanged.onNext(true)
                    }
                    GLFW.GLFW_KEY_KP_ADD -> pressAndHoldAction(event, rampDuration = 1500f)
                        .subscribe { gameState.playerOnTurn!!.playerAim.addPrecision() }
                    GLFW.GLFW_KEY_KP_SUBTRACT -> pressAndHoldAction(event, rampDuration = 1500f)
                        .subscribe { gameState.playerOnTurn!!.playerAim.addPrecision(-1f) }
                    GLFW.GLFW_KEY_ESCAPE -> when (currentPhase) {
                        MAIN_MENU -> dI.window.exit()
                        else -> setupMainMenu()
                    }
                }
            }
        }
    }

    fun eventLeftClick(startEvent: MouseButtonEvent, event: Observable<MouseButtonEvent>) {
        when {
            mouseElementPhases.any { currentPhase == it } -> guiController.sendLeftClick(
                startEvent.toScreen(), event.doOnNext { it.toScreen() }.share())
            currentPhase == PLAYERS_TURN_AIMING -> currentPhase = PLAYERS_TURN
            currentPhase == PLAYERS_TURN_POWERING -> currentPhase = PLAYERS_TURN
            currentPhase == PLAYERS_TURN_JUMPED -> {
                checkNotNull(gameState.playerOnTurn) { "No player is on turn" }
                val playerOnTurn = gameState.playerOnTurn!!

                playerOnTurn.thrustVehicle(event.takeWhile { currentPhase == PLAYERS_TURN_JUMPED })
            }
        }
    }

    fun eventRightClick(startEvent: MouseButtonEvent, event: Observable<MouseButtonEvent>) {
        val screenLocation = camera.getScreenLocation(startEvent.location)
        if (guiController.locationIsGui(screenLocation)) {

        } else {
            var lastLocation = startEvent.location
            event.subscribe {
                val newLocation = it.location
                val movement = camera.getWorldLocation(lastLocation)
                    .sub(camera.getWorldLocation(newLocation))
                camera.moveLocation(movement)
                lastLocation = newLocation
            }
        }
    }

    fun dispose() {
        guiController.dispose()
    }

    companion object {

        private const val pauseTime = 1000f
        private const val introDuration = 5000f
        private const val introTimeEnd = 2000f
        private const val maxTurnDuration = 16_000f
        private const val maxJumpDuration = 26_000f
        private const val quickTimeStart = 300f
        private const val jumpTimeStart = 600f
        private const val jumpTimeSafeDuration = 2000f
        private const val stabilizeTimeDuration = 4000f
        private const val outroDuration = 5000f
        private const val cameraPanDuration = 700f
        private const val watchHpDropDuration = 1500f
        private const val watchHpIdleDuration = 500f

        private val mouseElementPhases = listOf(
            MAIN_MENU,
            MAIN_MENU_SELECT_PLAYERS,
            PLAYERS_PICK_SHIELDS,
            PLAYERS_TURN,
            END_ROUND
        )

    }

}
