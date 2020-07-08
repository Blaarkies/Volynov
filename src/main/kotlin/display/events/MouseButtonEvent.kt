package display.events

import dI
import org.jbox2d.common.Vec2

class MouseButtonEvent(
    val button: Int,
    val action: Int,
    val mods: Int,
    val location: Vec2) {

    fun toScreen(): MouseButtonEvent {
        location.set(dI.cameraView.getScreenLocation(location))
        return this
    }

    fun clone(): MouseButtonEvent = MouseButtonEvent(button, action, mods, location.clone())

}
