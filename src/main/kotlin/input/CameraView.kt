package input

import display.Window
import engine.freeBody.FreeBody
import org.jbox2d.common.Vec2
import utility.PidController
import utility.PidControllerVec2
import utility.math.Matrix4f
import kotlin.math.absoluteValue
import kotlin.math.pow

class CameraView(private val window: Window) {

    val windowWidth: Float
        get() = window.width.toFloat()
    val windowHeight: Float
        get() = window.height.toFloat()

    var location = Vec2()
    var z = .05f

    private var targetZ = z
    private val zoomController = PidController(-.08f, -.0001f, -.05f)

    private var targetLocation = location
    private val movementController = PidControllerVec2(-.06f, -.00025f, -.01f)

    fun update() {
        if (z.minus(targetZ).absoluteValue > .0001f) {
            z += zoomController.getReaction(z, targetZ)
        }
        if (location.sub(targetLocation).length() > .01f) {
            location.addLocal(movementController.getReaction(location, targetLocation))
        }
    }

    fun setNewLocation(position: Vec2) {
        targetLocation = position
    }

    fun trackFreeBody(newFreeBody: FreeBody) {
        targetLocation = newFreeBody.worldBody.position
    }

    fun moveLocation(movement: Vec2) {
        location.addLocal(movement)
        targetLocation = location
        movementController.reset()
    }

    fun moveZoom(movement: Float) {
        targetZ = (targetZ + movement * targetZ.pow(1.2f) * 50f).coerceIn(.01f, .15f)
    }

    fun reset() {
        targetLocation = Vec2()
        targetZ = .05f
    }

    fun getRenderCamera(): Matrix4f {
        val zoomScale = 1f / z
        return Matrix4f.scale(zoomScale, zoomScale, 1f)
            .multiply(Matrix4f.translate(-location.x, -location.y, 0f))
    }

    fun getScreenLocation(cursorLocation: Vec2): Vec2 =
        cursorLocation.add(Vec2(-windowWidth, -windowHeight).mul(.5f)).also { it.y *= -1f }

    fun getWorldLocation(screenLocation: Vec2): Vec2 = getScreenLocation(screenLocation).mul(z).add(location)

    fun getGuiLocation(worldLocation: Vec2): Vec2 = worldLocation.add(location.negate()).mul(1f / z)

}
