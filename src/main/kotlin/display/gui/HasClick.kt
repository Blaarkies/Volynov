package display.gui

import org.jbox2d.common.Vec2

interface HasClick : HasHover {

    var isPressed: Boolean
    val onClick: () -> Unit

    override fun handleHover(location: Vec2): Boolean {
        if (isPressed) return false
        return super.handleHover(location)
    }

    fun handleLeftClickPress(location: Vec2): Boolean {
        val isHovered = isHover(location)
        if (isHovered) {
            isPressed = true
            currentPhase = GuiElementPhases.ACTIVE
        }
        return isHovered
    }

    fun handleLeftClickRelease(location: Vec2): Boolean {
        if (!isPressed) return false

        if (isHover(location)) {
            onClick()
        }
        isPressed = false
        currentPhase = GuiElementPhases.IDLE
        return true
    }
}
