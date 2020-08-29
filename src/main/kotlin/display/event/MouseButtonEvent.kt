package display.event

import dI
import org.jbox2d.common.Vec2
import org.lwjgl.glfw.GLFW

class MouseButtonEvent(
    val button: Int,
    override val action: Int,
    val mods: Int,
    val location: Vec2): ButtonPress {

    val isLeft: Boolean
        get() = button == GLFW.GLFW_MOUSE_BUTTON_LEFT

    val isRight: Boolean
        get() = button == GLFW.GLFW_MOUSE_BUTTON_RIGHT

    fun toScreen(): MouseButtonEvent {
        location.set(dI.cameraView.getScreenLocation(location))
        return this
    }

    fun clone(): MouseButtonEvent = MouseButtonEvent(button, action, mods, location.clone())

}
