package display

import engine.freeBody.FreeBody
import org.jbox2d.common.Vec2
import utility.Common.getTimingFunctionFullSine
import utility.Common.getTimingFunctionSigmoid
import utility.Common.getTimingFunctionSineEaseIn
import java.lang.Math.pow
import kotlin.math.pow
import kotlin.math.sqrt

class CameraView(private val window: Window) {

    val windowWidth: Float
        get() = window.width.toFloat()
    val windowHeight: Float
        get() = window.height.toFloat()

    var location = Vec2(0f, 0f)
    var z = .05f

    var currentPhase = CameraPhases.STATIC
    var lastStaticLocation = location
    private var lastPhaseTimestamp = System.currentTimeMillis()
    private val transitionDuration = 1000f

    private lateinit var trackFreeBody: FreeBody

    fun update() {
        when (currentPhase) {
            CameraPhases.TRANSITION_TO_TARGET -> {
                val interpolateStep = (System.currentTimeMillis() - lastPhaseTimestamp) / transitionDuration
                if (interpolateStep >= 1f) {
                    location = trackFreeBody.worldBody.position
                    currentPhase = CameraPhases.TRACKING_TARGET
                } else {
                    val timeFunctionStep = getTimingFunctionSigmoid(interpolateStep, 3f)
                    setNewLocation(
                        trackFreeBody.worldBody.position.mul(timeFunctionStep)
                            .add(lastStaticLocation.mul(1f - timeFunctionStep))
                    )
                }
            }
            else -> return
        }
    }

    fun setNewLocation(position: Vec2) {
        location = position
    }

    fun trackFreeBody(newFreeBody: FreeBody) {
        currentPhase = CameraPhases.TRANSITION_TO_TARGET
        lastPhaseTimestamp = System.currentTimeMillis()
        trackFreeBody = newFreeBody

        lastStaticLocation = location
    }

    fun moveLocation(movement: Vec2) {
        location = location.add(movement)
    }

    fun moveZoom(movement: Float) {
        z = (z + movement * z.pow(1.2f) * 50f).coerceIn(.0001f, 1f)
    }


}

enum class CameraPhases {
    STATIC,
    TRANSITION_TO_TARGET,
    TRACKING_TARGET
}
