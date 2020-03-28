package game

import display.draw.Drawer
import display.draw.TextureHolder
import display.KeyboardEvent
import display.Window
import display.events.MouseButtonEvent
import display.graphic.Color
import display.gui.GuiController
import engine.GameState
import engine.shields.VehicleShield
import org.jbox2d.common.Vec2
import utility.Common.getTimingFunctionEaseOut
import utility.Common.getTimingFunctionSineEaseIn
import utility.Common.vectorUnit
import kotlin.math.roundToInt

class GamePhaseHandler(private val gameState: GameState, val drawer: Drawer, val textures: TextureHolder) {

    private val timeStep = 1f / 60f
    private val velocityIterations = 8
    private val positionIterations = 3

    private val camera
        get() = gameState.camera

    private var currentPhase = GamePhases.NONE
    private var isTransitioning = false

    private val currentTime
        get() = System.currentTimeMillis()

    private val elapsedTime
        get() = (currentTime - lastPhaseTimestamp)

    private var lastPhaseTimestamp = currentTime

    private val guiController = GuiController(drawer)
    private lateinit var exitCall: () -> Unit

    fun init(window: Window) {
        exitCall = { window.exit() }

        setupMainMenu()

//        gameState.gamePlayers.add(GamePlayer("Bob"))
//        setupStartGame()
    }

    fun dragMouseRightClick(movement: Vec2) {
        camera.moveLocation(movement.mulLocal(-camera.z))
    }

    fun scrollCamera(movement: Float) {
        camera.moveZoom(movement * -.001f)
    }

    fun pauseGame(event: KeyboardEvent) {
        when (currentPhase) {
            GamePhases.PAUSE -> currentPhase = GamePhases.PLAY
            GamePhases.PLAY -> currentPhase = GamePhases.PAUSE
        }
        startTransition()
    }

    private fun startTransition() {
        lastPhaseTimestamp = currentTime
        isTransitioning = true
    }

    fun update() {
        camera.update()

        val cp = currentPhase
        when {
            cp == GamePhases.PAUSE && isTransitioning -> tickGamePausing(pauseDownDuration, GamePhases.PAUSE)
            cp == GamePhases.PAUSE -> return
            cp == GamePhases.PLAY && isTransitioning -> tickGameUnpausing(pauseDownDuration, GamePhases.PLAY)
            cp == GamePhases.MAIN_MENU -> return
            cp == GamePhases.MAIN_MENU_SELECT_PLAYERS -> return
            cp == GamePhases.NEW_GAME_INTRO && isTransitioning -> tickGameUnpausing(
                pauseDownDuration, GamePhases.NEW_GAME_INTRO
            )
            cp == GamePhases.NEW_GAME_INTRO -> handleIntro()
            cp == GamePhases.PLAYERS_PICK_SHIELDS && isTransitioning -> {
                if (elapsedTime < pauseDownDuration) isTransitioning = false
            }
            cp == GamePhases.PLAYERS_PICK_SHIELDS -> return
            cp == GamePhases.PLAYERS_TURN -> return

            else -> gameState.tickClock(timeStep, velocityIterations, positionIterations)
        }
    }

    private fun handleIntro() {
        when {
            elapsedTime > introDuration -> playerSelectsShield()
            elapsedTime > (introDuration - introStartSlowdown) -> tickGamePausing(
                introStartSlowdown, GamePhases.PLAYERS_PICK_SHIELDS, (elapsedTime - introDuration + introStartSlowdown)
            )
            else -> gameState.tickClock(timeStep, velocityIterations, positionIterations)
        }
    }

    private fun playerSelectsShield(player: GamePlayer? = null) {
        player?.vehicle?.shield = VehicleShield()

        if (gameState.gamePlayers.all { it.vehicle?.shield != null }) {
            setupPlayersTurn()
            return
        }
        guiController.clear()
        setNextPlayerOnTurn()
        setupPlayersPickShields()

        currentPhase = GamePhases.PLAYERS_PICK_SHIELDS
        startTransition()
    }

    private fun setupPlayersTurn() {
        guiController.clear()
        setNextPlayerOnTurn()
        setupPlayerCommandPanel()

        currentPhase = GamePhases.PLAYERS_TURN
        startTransition()
    }

    private fun setupPlayerCommandPanel() {
        guiController.clear()
        guiController.createPlayerCommandPanel(
            player = gameState.playerOnTurn!!,
            onClickFire = { player -> playerFires(player) }
        )
    }

    private fun playerFires(player: GamePlayer) {
        println("Player ${player.name} fired a gun!")
    }

    private fun setNextPlayerOnTurn() {
        check(gameState.playerOnTurn != null) { "Cannot play a game with no players." }
        val playerOnTurn = gameState.playerOnTurn!!
        val players = gameState.gamePlayers
        gameState.playerOnTurn = players[(players.indexOf(playerOnTurn) + 1).rem(players.size)]

        camera.trackFreeBody(gameState.playerOnTurn!!.vehicle!!)
    }

    private fun setupPlayersPickShields() {
        guiController.clear()
        guiController.createPlayersPickShields(
            onClickShield = { player -> playerSelectsShield(player) },
            player = gameState.playerOnTurn!!
        )
    }

    fun render() {
        when (currentPhase) {
            GamePhases.MAIN_MENU -> guiController.render()
            GamePhases.MAIN_MENU_SELECT_PLAYERS -> guiController.render()
            GamePhases.PLAYERS_PICK_SHIELDS -> {
                drawPlayPhase()
                guiController.render()
            }
            GamePhases.PLAYERS_TURN -> {
                drawPlayPhase()
                guiController.render()
            }
            else -> drawPlayPhase()
        }

        drawer.renderer.drawText(
            "Animating: ${isTransitioning.toString().padEnd(5, ' ')} ${currentPhase.name}",
            Vec2(120f - camera.windowWidth * .5f, -10f + camera.windowHeight * .5f),
            vectorUnit.mul(0.1f), Color.GREEN, false
        )

        drawer.renderer.drawText(
            "${elapsedTime.div(100f).roundToInt().div(10f)} seconds",
            Vec2(40f - camera.windowWidth * .5f, -30f + camera.windowHeight * .5f),
            vectorUnit.mul(0.1f), Color.GREEN, false
        )
    }

    private fun drawPlayPhase() {
        drawer.drawPicture(textures.stars_2k)

        val allFreeBodies = gameState.tickables
        allFreeBodies.forEach { drawer.drawTrail(it) }
        allFreeBodies.forEach { drawer.drawFreeBody(it) }
        //        allFreeBodies.forEach { drawDebugForces(it) }
        //        drawer.drawGravityCells(gameState.gravityMap, gameState.resolution)
    }

    private fun tickGameUnpausing(duration: Float = pauseDownDuration, endPhase: GamePhases) {
        val interpolateStep = elapsedTime / duration
        if (interpolateStep >= 1f) {
            currentPhase = endPhase
            isTransitioning = false
        } else {
            val timeFunctionStep = getTimingFunctionEaseOut(interpolateStep)
            gameState.tickClock(timeStep * timeFunctionStep, velocityIterations, positionIterations)
        }
    }

    private fun tickGamePausing(
        duration: Float = pauseDownDuration,
        endPhase: GamePhases,
        calculatedElapsedTime: Float? = null
    ) {
        val interpolateStep = (calculatedElapsedTime ?: elapsedTime.toFloat()) / duration
        if (interpolateStep >= 1f) {
            currentPhase = endPhase
            isTransitioning = false
        } else {
            val timeFunctionStep = getTimingFunctionSineEaseIn(1f - interpolateStep)
            gameState.tickClock(timeStep * timeFunctionStep, velocityIterations, positionIterations)
        }
    }

    fun doubleLeftClick(location: Vec2, click: MouseButtonEvent) {
        val transformedLocation = getScreenLocation(location).mul(camera.z).add(camera.location)

        val clickedBody = gameState.tickables.find {
            it.worldBody.position
                .add(transformedLocation.mul(-1f))
                .length() <= it.radius
        }
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
        if (mouseElementPhases.any { currentPhase == it }) {
            guiController.checkHover(getScreenLocation(location))
        }
    }

    fun leftClickMouse(event: MouseButtonEvent) {
        if (mouseElementPhases.any { currentPhase == it }) {
            guiController.checkLeftClick(getScreenLocation(event.location))
        }
    }

    private fun getScreenLocation(location: Vec2): Vec2 =
        location.add(Vec2(-camera.windowWidth * .5f, -camera.windowHeight * .5f))
            .let {
                it.y *= -1f
                it
            }

    fun keyPressEscape(event: KeyboardEvent) {
        when (currentPhase) {
            GamePhases.MAIN_MENU -> exitCall()
            else -> setupMainMenu()
        }
    }

    private fun setupMainMenu() {
        currentPhase = GamePhases.MAIN_MENU
        guiController.clear()
        guiController.createMainMenu(
            onClickNewGame = { setupMainMenuSelectPlayers() },
            onClickSettings = {},
            onClickQuit = exitCall
        )
    }

    private fun setupMainMenuSelectPlayers() {
        currentPhase = GamePhases.MAIN_MENU_SELECT_PLAYERS
        gameState.reset()
        guiController.clear()
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
        currentPhase = GamePhases.NEW_GAME_INTRO
        startTransition()

        MapGenerator.populateNewGameMap(gameState, textures)

        if (gameState.gamePlayers.size > 0) {
            val startingPlayer = gameState.gamePlayers.random()
            gameState.playerOnTurn = startingPlayer
        }
    }

    companion object {

        private const val pauseDownDuration = 1000f
        private const val introDuration = 5000f
        private const val introStartSlowdown = 2000f
        private const val maxPlayDuration = 30000f
        private val mouseElementPhases = listOf(
            GamePhases.MAIN_MENU,
            GamePhases.MAIN_MENU_SELECT_PLAYERS,
            GamePhases.PLAYERS_PICK_SHIELDS,
            GamePhases.PLAYERS_TURN
        )

    }

}
