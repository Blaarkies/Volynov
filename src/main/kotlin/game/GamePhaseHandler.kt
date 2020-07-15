package game

import dI
import display.events.*
import display.graphic.Color
import display.text.TextJustify
import engine.gameState.GameStateSimulator.getNewPrediction
import engine.motion.Director
import engine.shields.VehicleShield
import game.GamePhases.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.jbox2d.common.Vec2
import utility.Common.getTimingFunctionEaseIn
import utility.Common.getTimingFunctionEaseOut
import utility.Common.vectorUnit
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
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

    private fun startNewPhase(newPhase: GamePhases) {
        currentPhase = newPhase
        startTransition()
    }

    fun update() {
        camera.update()

        val cp = currentPhase
        when {
            cp == PAUSE && isTransitioning -> tickGamePausing()
            cp == PAUSE -> return
            cp == PLAY && isTransitioning -> tickGameUnpausing()
            cp == MAIN_MENU -> return
            cp == MAIN_MENU_SELECT_PLAYERS -> return
            cp == NEW_GAME_INTRO && isTransitioning -> tickGameUnpausing()
            cp == NEW_GAME_INTRO -> handleIntro()
            cp == PLAYERS_PICK_SHIELDS && isTransitioning -> {
                if (elapsedTime > pauseTime) isTransitioning = false
            }
            cp == PLAYERS_PICK_SHIELDS -> guiController.update()
            cp == PLAYERS_TURN -> guiController.update()
            cp == PLAYERS_TURN_FIRED && isTransitioning -> tickGameUnpausing(quickTimeStart)
            cp == PLAYERS_TURN_FIRED -> handlePlayerShot()
            cp == PLAYERS_TURN_JUMPED && isTransitioning -> tickGameUnpausing(jumpTimeStart)
            cp == PLAYERS_TURN_JUMPED -> handlePlayerShot()
            cp == PLAYERS_TURN_FIRED_ENDS_EARLY -> handlePlayerShotEndsEarly()
            cp == PLAYERS_TURN_AIMING -> guiController.update()
            cp == PLAYERS_TURN_POWERING -> guiController.update()
            cp == END_ROUND && isTransitioning -> tickGamePausing(outroDuration, endSpeed = .1f)
            cp == END_ROUND -> gameState.tickClock(timeStep * .1f, velocityIterations, positionIterations)

            else -> gameState.tickClock(timeStep, velocityIterations, positionIterations)
        }
    }

    fun render() {
        when (currentPhase) {
            MAIN_MENU -> guiController.render()
            MAIN_MENU_SELECT_PLAYERS -> guiController.render()
            PLAYERS_PICK_SHIELDS -> drawWorldAndGui()
            PLAYERS_TURN -> {
                drawWorldAndGui()
                gameState.gravityBodies.forEach { drawer.drawMotionPredictors(it) }
            }
            PLAYERS_TURN_AIMING -> {
                drawWorldAndGui()
                drawPlayerAimingGui()
            }
            PLAYERS_TURN_POWERING -> {
                drawWorldAndGui()
                drawPlayerAimingGui()
            }
            PLAYERS_TURN_JUMPED -> {
                drawPlayPhase()

                val fuelRemaining = dI.gameState.playerOnTurn!!.vehicle!!.fuel!!.amount.roundToInt()
                drawer.renderer.drawText(
                    "$fuelRemaining fuel remaining",
                    Vec2(100f, 0f),
                    vectorUnit.mul(0.1f),
                    Color.WHITE,
                    useCamera = false)
                //                drawer.renderer.drawText("x", dI.gameState.playerOnTurn!!.vehicle!!.thrustTarget, vectorUnit.mul(0.01f),
                //                    Color.WHITE, useCamera = true)
            }
            END_ROUND -> drawWorldAndGui()
            else -> drawPlayPhase()
        }

        val debugColor = Color.GREEN.setAlpha(.7f)
        drawer.renderer.drawText(
            "${if (isTransitioning) "Active" else "Idle  "} / Phase ${currentPhase.name}",
            Vec2(5f - camera.windowWidth * .5f, -10f + camera.windowHeight * .5f),
            vectorUnit.mul(0.1f), debugColor, TextJustify.LEFT, false
        )

        drawer.renderer.drawText(
            "GameTime ${gameState.tickTime.div(100f).roundToInt().div(10f)}s / PhaseTime ${elapsedTime.div(100f)
                .roundToInt().div(10f)}s",
            Vec2(5f - camera.windowWidth * .5f, -30f + camera.windowHeight * .5f),
            vectorUnit.mul(0.1f), debugColor, TextJustify.LEFT, false
        )
    }

    private fun drawPlayerAimingGui() {
        drawer.drawPlayerAimingPointer(gameState.playerOnTurn!!)
        drawer.drawWarheadTrajectory(latestPrediction)
        gameState.gravityBodies.forEach { drawer.drawMotionPredictors(it) }
    }

    private fun handlePlayerShotEndsEarly() {
        addPlayerLabels()
        when {
            isTransitioning -> tickGamePausing()
            else -> setupNextPlayersFireTurn()
        }
    }

    private fun handlePlayerShot() {
        val roundEndsEarly = (gameState.warheads.none()
                && gameState.particles.none()
                && gameState.vehicles.all { it.isStable })
        when {
            roundEndsEarly -> if (!checkStateEndOfRound()) startNewPhase(PLAYERS_TURN_FIRED_ENDS_EARLY)
            elapsedTime > maxTurnDuration -> setupNextPlayersFireTurn()
            elapsedTime > (maxTurnDuration - pauseTime) -> tickGamePausing(
                pauseTime, calculatedElapsedTime = (elapsedTime - maxTurnDuration + pauseTime)
            )
            else -> gameState.tickClock(timeStep, velocityIterations, positionIterations)
        }
    }

    private fun handleIntro() {
        when {
            elapsedTime > introDuration -> playerSelectsShield()
            elapsedTime > (introDuration - introTimeEnd) -> tickGamePausing(
                introTimeEnd, calculatedElapsedTime = (elapsedTime - introDuration + introTimeEnd)
            )
            else -> gameState.tickClock(timeStep, velocityIterations, positionIterations)
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
        //        startTransition()
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
            startNewPhase(END_ROUND)
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

        player.startJump()

        startNewPhase(PLAYERS_TURN_JUMPED)
    }

    private fun playerFires(player: GamePlayer) {
        guiController.clear()
        // check() {} player has enough funds && in stable position to fire large warheads

        player.vehicle?.fireWarhead(gameState, player, "boom small") { warhead -> camera.trackFreeBody(warhead) }

        startNewPhase(PLAYERS_TURN_FIRED)
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
        endPhase: GamePhases? = null,
        calculatedElapsedTime: Float? = null
    ) {
        val interpolateStep = (calculatedElapsedTime ?: elapsedTime.toFloat()) / duration
        if (interpolateStep >= 1f) {
            currentPhase = endPhase ?: currentPhase
            isTransitioning = false
        } else {
            val timeFunctionStep = getTimingFunctionEaseOut(interpolateStep)
            gameState.tickClock(timeStep * timeFunctionStep, velocityIterations, positionIterations)
        }
    }

    private fun tickGamePausing(
        duration: Float = pauseTime,
        endPhase: GamePhases? = null,
        calculatedElapsedTime: Float? = null,
        endSpeed: Float = 0f
    ) {
        val interpolateStep = (calculatedElapsedTime ?: elapsedTime.toFloat()) / duration
        if (interpolateStep >= 1f) {
            currentPhase = endPhase ?: currentPhase
            isTransitioning = false
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

                playerOnTurn.thrustVehicle(event)
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
        private const val introDuration = 3500f
        private const val introTimeEnd = 2000f
        private const val maxTurnDuration = 20000f
        private const val quickTimeStart = 300f
        private const val jumpTimeStart = 600f
        private const val outroDuration = 5000f

        private val mouseElementPhases = listOf(
            MAIN_MENU,
            MAIN_MENU_SELECT_PLAYERS,
            PLAYERS_PICK_SHIELDS,
            PLAYERS_TURN,
            END_ROUND
        )

    }

}
