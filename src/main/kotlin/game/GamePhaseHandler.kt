package game

import display.draw.Drawer
import display.draw.TextureHolder
import display.KeyboardEvent
import display.MouseButtonEvent
import engine.GameState
import org.jbox2d.common.Vec2
import utility.Common.getTimingFunctionEaseOut
import utility.Common.getTimingFunctionSineEaseIn

class GamePhaseHandler(private val gameState: GameState, val drawer: Drawer, val textures: TextureHolder) {

    private val timeStep = 1f / 60f
    private val velocityIterations = 8
    private val positionIterations = 3

    private val camera
        get() = gameState.camera

    private var currentPhase = GamePhases.NONE
    private var lastPhaseTimestamp = System.currentTimeMillis()
    private val pauseDownDuration = 1000f

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
            else -> gameState.tickClock(timeStep, velocityIterations, positionIterations)
        }
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

}
