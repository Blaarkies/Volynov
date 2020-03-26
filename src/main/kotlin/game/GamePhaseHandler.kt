package game

import display.draw.Drawer
import display.draw.TextureHolder
import display.KeyboardEvent
import display.Window
import display.events.MouseButtonEvent
import display.gui.GuiController
import engine.GameState
import engine.freeBody.FreeBody
import org.jbox2d.common.Vec2
import utility.Common.getTimingFunctionEaseOut
import utility.Common.getTimingFunctionSineEaseIn

class GamePhaseHandler(private val gameState: GameState, val drawer: Drawer, val textures: TextureHolder) {

    private val timeStep = 1f / 60f
    private val velocityIterations = 8
    private val positionIterations = 3

    private val camera
        get() = gameState.camera

    private var currentPhase = GamePhases.MAIN_MENU
    private var isTransitioning = false
    private var lastPhaseTimestamp = System.currentTimeMillis()
    private val pauseDownDuration = 1000f

    private val guiController = GuiController(drawer)
    private lateinit var exitCall: () -> Unit

    fun init(window: Window) {
        exitCall = { window.exit() }

        setupMainMenu()
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
//        gameState.gamePlayers.add(GamePlayer(gameState.gamePlayers.size.toString()))
        guiController.updateMainMenuSelectPlayers(gameState.gamePlayers, { onAddPlayerButton() },
            { onRemovePlayerButton() })
    }

    private fun setupStartGame() {
        currentPhase = GamePhases.NEW_GAME_INTRO

        MapGenerator.populateNewGameMap(gameState, textures)
        camera.trackFreeBody(gameState.tickables.maxBy { it.worldBody.mass } as FreeBody)

        if (gameState.gamePlayers.size > 0) {
            val startingPlayer = gameState.gamePlayers.random()
            gameState.playerOnTurn = startingPlayer
        }

//        gameState.gamePlayers.
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
        lastPhaseTimestamp = System.currentTimeMillis()
        isTransitioning = true
    }

    fun update() {
        camera.update()

        when {
            currentPhase == GamePhases.PAUSE && isTransitioning -> tickGamePausing()
            currentPhase == GamePhases.PAUSE -> return
            currentPhase == GamePhases.PLAY && isTransitioning -> tickGameUnpausing()
            currentPhase == GamePhases.MAIN_MENU -> return
            currentPhase == GamePhases.MAIN_MENU_SELECT_PLAYERS -> return
            else -> gameState.tickClock(timeStep, velocityIterations, positionIterations)
        }
    }

    fun render() {
        when (currentPhase) {
            GamePhases.MAIN_MENU -> guiController.render()
            GamePhases.MAIN_MENU_SELECT_PLAYERS -> guiController.render()
            else -> drawPlayPhase()
        }
    }

    private fun drawPlayPhase() {
        drawer.drawPicture(textures.stars_2k)

        val allFreeBodies = gameState.tickables
        allFreeBodies.forEach { drawer.drawTrail(it) }
        allFreeBodies.forEach { drawer.drawFreeBody(it) }
        //        allFreeBodies.forEach { drawDebugForces(it) }
        //        drawer.drawGravityCells(gameState.gravityMap, gameState.resolution)
    }

    private fun tickGameUnpausing() {
        val interpolateStep = (System.currentTimeMillis() - lastPhaseTimestamp) / pauseDownDuration
        if (interpolateStep >= 1f) {
            currentPhase = GamePhases.PLAY
        } else {
            val timeFunctionStep = getTimingFunctionEaseOut(interpolateStep)
            gameState.tickClock(timeStep * timeFunctionStep, velocityIterations, positionIterations)
        }
    }

    private fun tickGamePausing() {
        val interpolateStep = (System.currentTimeMillis() - lastPhaseTimestamp) / pauseDownDuration
        if (interpolateStep >= 1f) {
            currentPhase = GamePhases.PAUSE
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
        val transformedLocation = getScreenLocation(location)

        when (currentPhase) {
            GamePhases.MAIN_MENU -> guiController.checkHover(transformedLocation)
            GamePhases.MAIN_MENU_SELECT_PLAYERS -> guiController.checkHover(transformedLocation)
        }
    }

    fun leftClickMouse(event: MouseButtonEvent) {
        val transformedLocation = getScreenLocation(event.location)

        when (currentPhase) {
            GamePhases.MAIN_MENU -> guiController.checkLeftClick(transformedLocation)
            GamePhases.MAIN_MENU_SELECT_PLAYERS -> guiController.checkLeftClick(transformedLocation)
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

}
