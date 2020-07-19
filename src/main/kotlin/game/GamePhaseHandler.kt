package game

import dI
import display.events.*
import display.graphic.Color
import display.gui.elements.GuiLabel
import display.text.TextJustify
import engine.freeBody.Vehicle
import engine.freeBody.Warhead
import engine.gameState.GameStateSimulator.getNewPrediction
import engine.motion.Director
import engine.shields.VehicleShield
import game.GamePhase.*
import game.fuel.Fuel
import game.fuel.FuelType
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.jbox2d.common.Vec2
import utility.Common.getTimingFunctionEaseIn
import utility.Common.getTimingFunctionEaseOut
import utility.Common.vectorUnit
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

    private val currentTime
        get() = System.currentTimeMillis()

    private val elapsedTime
        get() = (currentTime - lastPhaseTimestamp)

    private var lastPhaseTimestamp = currentTime

    var latestPrediction = TrajectoryPrediction(currentTime)
    var playerAimChanged = PublishSubject.create<Boolean>()
    private val unsubscribe = PublishSubject.create<Boolean>()

    private val phaseUpdateMap: HashMap<GamePhase, () -> Unit>
    private val phaseRenderMap: HashMap<GamePhase, () -> Unit>
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
            Pair(PLAYERS_TURN_POWERING, { guiController.update() }),
            Pair(PLAYERS_WATCH_DAMAGE_DEALT_INTRO, {
                guiController.update()
                waitOnPhase(1000f, PLAYERS_WATCH_DAMAGE_DEALT, true)
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
                gameState.gravityBodies.forEach { drawer.drawMotionPredictors(it) }
            }),
            Pair(PLAYERS_TURN_AIMING, {
                drawWorldAndGui()
                drawPlayerAimingGui()
            }),
            Pair(PLAYERS_TURN_POWERING, {
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
                gameState.gamePlayers.addAll((1..3).map { GamePlayer("Player $it", cash = 1000f) })
                MapGenerator.populateNewGameMap(gameState)

                gameState.gamePlayers.forEach { it.vehicle?.shield = VehicleShield() }
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
        lastPhaseTimestamp = currentTime
        isTransitioning = true
    }

    private fun startNewPhase(newPhase: GamePhase) {
        currentPhase = newPhase
        startTransition()
    }

    fun update() {
        camera.update()

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
            "GameTime ${gameState.tickTime.div(100f).roundToInt().div(10f)}s / PhaseTime ${elapsedTime.div(100f)
                .roundToInt().div(10f)}s",
            Vec2(5f - camera.windowWidth * .5f, -30f + camera.windowHeight * .5f),
            vectorUnit.mul(0.1f), debugColor, TextJustify.LEFT, false)
    }

    private fun drawPlayerAimingGui() {
        drawer.drawPlayerAimingPointer(gameState.playerOnTurn!!)
        drawer.drawWarheadTrajectory(latestPrediction)
        gameState.gravityBodies.forEach { drawer.drawMotionPredictors(it) }
    }

    private fun handleIntro() {
        when {
            isTransitioning -> tickGameUnpausing()
            elapsedTime > introDuration -> playerSelectsShield()
            elapsedTime > (introDuration - introTimeEnd) -> tickGamePausing(
                introTimeEnd, calculatedElapsedTime = (elapsedTime - introDuration + introTimeEnd))
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
            elapsedTime > maxTurnDuration || roundEndsEarly() -> startNewPhase(PLAYERS_TURN_FIRED_WAIT_STABILIZE)
            else -> tickClockNormalSpeed()
        }
    }

    private fun endOfJump() = (elapsedTime > maxTurnDuration && gameState.playerOnTurn!!.vehicle!!.fuel!!.amount < .1f)
            || elapsedTime > maxJumpDuration
            || gameState.playerOnTurn!!.vehicle!!.hasCollided

    private fun handlePlayerJumped() {
        guiController.update()
        when {
            isTransitioning -> tickGameUnpausing(jumpTimeStart)
            elapsedTime < jumpTimeSafeDuration -> {
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
            elapsedTime > duration -> {
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

            camera.trackFreeBody(lastVehicleWatched!!)
            startNewPhase(PLAYERS_WATCH_DAMAGE_DEALT_INTRO)
        } else {
            setupNextPlayersFireTurn()
        }
    }

    private fun handlePlayerWatchDamageDealt() {
        val watchedVehicle = lastVehicleWatched!!
        when {
            isTransitioning && elapsedTime < cameraPanDuration -> return
            isTransitioning && elapsedTime < cameraPanDuration + watchHpDropDuration -> {
                val interpolationStep = 1f - (elapsedTime - cameraPanDuration) / watchHpDropDuration
                val range = lastVehicleDamageRecords[watchedVehicle]!! - watchedVehicle.hitPoints
                val hp = (watchedVehicle.hitPoints +
                        getTimingFunctionEaseIn(interpolationStep) * range).coerceAtLeast(0f)
                playerLabels!![watchedVehicle]?.updateManual(
                    callback = { element: GuiLabel ->
                        element.title = "HP ${ceil(hp).toInt()}"
                        element.color = Color.createFromHsv((hp / 100f) * .3f, .5f, .7f)
                    })
            }
            isTransitioning && elapsedTime < cameraPanDuration + watchHpDropDuration + watchHpIdleDuration -> return
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

    private fun playerSelectsShield(player: GamePlayer? = null) {
        player?.vehicle?.shield = VehicleShield()

        if (gameState.gamePlayers.all { it.vehicle?.shield != null }) {
            setupNextPlayersFireTurn()
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
            onClickPower = { startNewPhase(PLAYERS_TURN_POWERING) },
            onClickMove = { player -> playerJumps(player) },
            onClickFire = { player -> playerFires(player) }
        )
        addPlayerLabels()
    }

    private fun addPlayerLabels() {
        guiController.addPlayerLabels(gameState.gamePlayers.filter { it.vehicle!!.hitPoints > 0 }, camera)
    }

    private fun playerJumps(player: GamePlayer) {
        guiController.clear()

        val selectedFuel = Fuel.descriptor[player.playerAim.selectedFuel] ?: Fuel.descriptor[FuelType.Hydrazine]!!
        player.buyItem(selectedFuel.name, selectedFuel.price, gameState.tickTime)

        recordLastVehicleHp()

        player.startJump()
        startNewPhase(PLAYERS_TURN_JUMPED)
        guiController.createJumpFuelBar(player)
    }

    private fun playerFires(player: GamePlayer) {
        guiController.clear()
        // check() {} player has enough funds && in stable position to fire large warheads
        recordLastVehicleHp()

        player.vehicle?.fireWarhead(gameState, player, "boom small") { warhead -> camera.trackFreeBody(warhead) }

        startNewPhase(PLAYERS_TURN_FIRED)
    }

    private fun recordLastVehicleHp() {
        lastVehicleDamageRecords = HashMap(gameState.vehicles.map { Pair(it, it.hitPoints) }.toMap())
    }

    private fun setNextPlayerOnTurn() {
        checkNotNull(gameState.playerOnTurn) { "No player is on turn" }
        val playerOnTurn = gameState.playerOnTurn!!
        val players = gameState.gamePlayers.filter { it.vehicle!!.hitPoints > 0 }
        gameState.playerOnTurn = players[(players.indexOf(playerOnTurn) + 1).rem(players.size)]

        camera.trackFreeBody(gameState.playerOnTurn!!.vehicle!!)
    }

    private fun setupPlayersPickShields() {
        guiController.createPlayersPickShields(
            onClickShield = { player -> playerSelectsShield(player) },
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
        allFreeBodies.forEach { drawer.drawTrail(it) }
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
        val interpolateStep = (calculatedElapsedTime ?: elapsedTime.toFloat()) / duration
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
        val interpolateStep = (calculatedElapsedTime ?: elapsedTime.toFloat()) / duration
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
            camera.moveZoom(event.movement.y * -.005f)
            guiController.update()
        }
    }

    fun pauseGame(event: KeyboardEvent) {
        currentPhase = when (currentPhase) {
            PAUSE -> PLAY
            PLAY -> PAUSE
            else -> PAUSE
        }
        startTransition()
    }

    fun doubleLeftClick(location: Vec2) {
        val transformedLocation = camera.getWorldLocation(location)

        val clickedBody = gameState.gravityBodies
            .find { it.worldBody.position.add(transformedLocation.mul(-1f)).length() <= it.radius }
            ?: gameState.gravityBodies
                .map { Pair(it, it.worldBody.position.add(transformedLocation.mul(-1f)).length()) }
                .filter { (body, distance) -> distance <= body.radius + 5f }
                .minBy { (_, distance) -> distance }?.first
            ?: return

        camera.trackFreeBody(clickedBody)
    }

    fun keyPressArrowLeft(event: KeyboardEvent) {
        drawer.renderer.debugOffset.addLocal(Vec2(-0.005f, 0f))
    }

    fun keyPressArrowRight(event: KeyboardEvent) {
        drawer.renderer.debugOffset.addLocal(Vec2(0.005f, 0f))
    }

    fun moveMouse(location: Vec2) {
        when {
            mouseElementPhases.any { currentPhase == it } -> guiController.checkHover(
                camera.getScreenLocation(location))
            currentPhase == PLAYERS_TURN_AIMING -> {
                val (playerOnTurn, transformedLocation, playerLocation) = getPlayerAndMouseLocations(location)
                val aimDirection = Director.getDirection(
                    transformedLocation.x, transformedLocation.y, playerLocation.x, playerLocation.y
                )
                playerOnTurn.playerAim.angle = aimDirection
                guiController.update()

                playerAimChanged.onNext(true)
            }
            currentPhase == PLAYERS_TURN_POWERING -> {
                val (playerOnTurn, transformedLocation, playerLocation) = getPlayerAndMouseLocations(location)
                val distance = Director.getDistance(
                    transformedLocation.x, transformedLocation.y, playerLocation.x, playerLocation.y
                )
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

    fun keyPressEscape(event: KeyboardEvent) {
        when (currentPhase) {
            MAIN_MENU -> dI.window.exit()
            else -> setupMainMenu()
        }
    }

    fun keyPressBackspace(event: KeyboardEvent) {
    }

    fun keyPressEnter(event: KeyboardEvent) {
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
        startNewPhase(NEW_GAME_INTRO)

        MapGenerator.populateNewGameMap(gameState)

        check(gameState.gamePlayers.size > 1) { "Cannot play a game with less than 2 players" }
        gameState.playerOnTurn = gameState.gamePlayers.random()
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
            val distanceCalculator = DistanceCalculator()
            event.subscribe {
                val movement = distanceCalculator.getLastDistance(it.toScreen().location)
                camera.moveLocation(movement.mulLocal(-camera.z))
            }
        }
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
