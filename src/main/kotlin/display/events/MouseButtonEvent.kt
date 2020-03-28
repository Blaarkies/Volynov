package display.events

import org.jbox2d.common.Vec2

class MouseButtonEvent(
    val button: Int,
    val action: Int,
    val mods: Int,
    val location: Vec2
)
