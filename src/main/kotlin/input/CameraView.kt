package input

import dI
import engine.freeBody.FreeBody
import org.jbox2d.common.Vec2
import utility.PidController
import utility.PidControllerVec2
import utility.StopWatch
import utility.math.Matrix4f
import kotlin.math.absoluteValue
import kotlin.math.pow

class CameraView {

    val windowWidth: Float
        get() = dI.window.width.toFloat()
    val windowHeight: Float
        get() = dI.window.height.toFloat()

    val windowWidthInt: Int
        get() = dI.window.width
    val windowHeightInt: Int
        get() = dI.window.height

    var location = Vec2()
    var z = .05f

    private var targetZ = z
    private val zoomController = PidController(-.06f, -.0001f, -.06f)

    private var targetVelocity = Vec2()
    private var targetVelocityAverage = Vec2()
    private var targetLocation = location
    private val normalMovementController = PidControllerVec2(-.06f, -.00025f, -.01f)
    private val slowMovementController = PidControllerVec2(-.02f, 0f, -.01f)

    private val stopWatch = StopWatch()

    fun update() {
        if (z.minus(targetZ).absoluteValue > .0001f) {
            z += zoomController.getReaction(z, targetZ)
        }
        val newTargetLocation = targetLocation.add(targetVelocityAverage.mul(.7f))
        if (location.sub(newTargetLocation).length() > .01f) {
            when {
                stopWatch.elapsedTime < 130f -> location.addLocal(slowMovementController.getReaction(location, newTargetLocation))
                else -> location.addLocal(normalMovementController.getReaction(location, newTargetLocation))
            }
        }
        targetVelocityAverage = targetVelocityAverage.mul(oldPortion).add(targetVelocity.mul(newPortion))
    }

    fun setNewLocation(position: Vec2) {
        targetLocation = position
        targetVelocity = Vec2()
    }

    fun trackFreeBody(newFreeBody: FreeBody) {
        targetLocation = newFreeBody.worldBody.position
        targetVelocity = newFreeBody.worldBody.linearVelocity
        targetVelocityAverage = Vec2()
        stopWatch.reset()
    }

    fun moveLocation(movement: Vec2) {
        location.addLocal(movement)
        targetLocation = location
        targetVelocity = Vec2()
        targetVelocityAverage = Vec2()
        normalMovementController.reset()
        slowMovementController.reset()
    }

    fun moveZoom(movement: Float) {
        targetZ = (targetZ + movement * targetZ.pow(1.2f) * 50f).coerceIn(.01f, .15f)
    }

    fun reset() {
        targetLocation = Vec2()
        targetZ = .05f
        targetVelocity = Vec2()
        targetVelocityAverage = Vec2()
        stopWatch.reset()
    }

    fun getRenderCamera(): Matrix4f {
        val zoomScale = 1f / z
        return Matrix4f.scale(zoomScale, zoomScale, 1f)
            .multiply(Matrix4f.translate(-location.x, -location.y, 0f))
    }

    fun getScreenLocation(cursorLocation: Vec2): Vec2 = cursorLocation
        .sub(Vec2(windowWidth, windowHeight).mul(.5f))
        .also { it.y *= -1f }

    fun getWorldLocation(screenLocation: Vec2): Vec2 = getScreenLocation(screenLocation).mul(z).add(location)

    fun getGuiLocation(worldLocation: Vec2): Vec2 = worldLocation.sub(location).mul(1f / z)

    companion object {

        const val newPortion = .05f
        const val oldPortion = 1f - newPortion

    }

}
