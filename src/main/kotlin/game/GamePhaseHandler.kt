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
    private var lastPhaseTimestamp = System.currentTimeMillis()
    private val pauseDownDuration = 1000f

    private val guiController = GuiController(drawer)
    private lateinit var exitCall: () -> Unit

    fun init(window: Window) {
        exitCall = { window.exit() }

        currentPhase = GamePhases.MAIN_MENU
        guiController.createMainMenu(
            onClickNewGame = {
                currentPhase = GamePhases.UNPAUSING
                gameState.reset()
                MapGenerator.populateTestMap(gameState, textures)
                gameState.camera.trackFreeBody(gameState.tickables.find { it.id == "terra" } as FreeBody)
            },
            onClickSettings = {},
            onClickQuit = { window.exit() }
        )
    }

    fun dragMouseRightClick(movement: Vec2) {
        gameState.camera.moveLocation(movement.mulLocal(-gameState.camera.z))
    }

    fun scrollCamera(movement: Float) {
        gameState.camera.moveZoom(movement * -.001f)
    }

    fun pauseGame(event: KeyboardEvent) {
        currentPhase = when (currentPhase) {
            GamePhases.NONE -> GamePhases.PAUSING
            GamePhases.PAUSED -> GamePhases.UNPAUSING
            GamePhases.UNPAUSED -> GamePhases.PAUSING
            else -> currentPhase
        }
        lastPhaseTimestamp = System.currentTimeMillis()
    }

    fun update() {
        camera.update()

        when (currentPhase) {
            GamePhases.PAUSING -> tickGamePausing()
            GamePhases.PAUSED -> return
            GamePhases.UNPAUSING -> tickGameUnpausing()
            GamePhases.MAIN_MENU -> return
            else -> gameState.tickClock(timeStep, velocityIterations, positionIterations)
        }
    }

    fun render() {
        when (currentPhase) {
            GamePhases.MAIN_MENU -> guiController.render()
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
            currentPhase = GamePhases.UNPAUSED
        } else {
            val timeFunctionStep = getTimingFunctionEaseOut(interpolateStep)
            gameState.tickClock(timeStep * (timeFunctionStep), velocityIterations, positionIterations)
        }
    }

    private fun tickGamePausing() {
        val interpolateStep = (System.currentTimeMillis() - lastPhaseTimestamp) / pauseDownDuration
        if (interpolateStep >= 1f) {
            currentPhase = GamePhases.PAUSED
        } else {
            val timeFunctionStep = getTimingFunctionSineEaseIn(1f - interpolateStep)
            gameState.tickClock(timeStep * timeFunctionStep, velocityIterations, positionIterations)
        }
    }

    fun doubleLeftClick(location: Vec2, click: MouseButtonEvent) {
        val transformedLocation = location.add(Vec2(-camera.windowWidth * .5f, -camera.windowHeight * .5f))
            .let {
                it.y *= -1f
                it.mul(camera.z).add(camera.location)
            }

        val clickedBody = gameState.tickables.find {
            it.worldBody.position
                .add(transformedLocation.mul(-1f))
                .length() <= it.radius
        }
            ?: return

        gameState.camera.trackFreeBody(clickedBody)
    }

    fun keyPressArrowLeft(event: KeyboardEvent) {
        drawer.renderer.debugOffset.addLocal(Vec2(-0.005f, 0f))
    }

    fun keyPressArrowRight(event: KeyboardEvent) {
        drawer.renderer.debugOffset.addLocal(Vec2(0.005f, 0f))
    }

    fun moveMouse(location: Vec2) {
        val transformedLocation = location.add(Vec2(-camera.windowWidth * .5f, -camera.windowHeight * .5f))
            .let {
                it.y *= -1f
                it
            }

        when (currentPhase) {
            GamePhases.MAIN_MENU -> guiController.checkHover(transformedLocation)
        }
    }

    fun leftClickMouse(event: MouseButtonEvent) {
        val transformedLocation = event.location.add(Vec2(-camera.windowWidth * .5f, -camera.windowHeight * .5f))
            .let {
                it.y *= -1f
                it
            }

        when (currentPhase) {
            GamePhases.MAIN_MENU -> guiController.checkLeftClick(transformedLocation)
        }
    }

    fun keyPressEscape(event: KeyboardEvent) {
        when (currentPhase) {
            GamePhases.MAIN_MENU -> exitCall()
            else -> currentPhase = GamePhases.MAIN_MENU
        }
    }

}
