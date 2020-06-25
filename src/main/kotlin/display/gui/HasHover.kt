package display.gui

import org.jbox2d.common.Vec2

interface HasHover : GuiElement {

    var topRight: Vec2
    var bottomLeft: Vec2

    override fun addOffset(movement: Vec2) {
        super.addOffset(movement)
        calculateElementRegion()
    }

    override fun updateOffset(newOffset: Vec2) {
        super.updateOffset(newOffset)
        calculateElementRegion()
    }

    fun isHover(location: Vec2): Boolean =
        isInRegion(location, bottomLeft, topRight)

    fun handleHover(location: Vec2) = when {
        isHover(location) -> {
            currentPhase = GuiElementPhases.HOVER
            true
        }
        else -> {
            currentPhase = GuiElementPhases.IDLE
            false
        }
    }

    fun calculateElementRegion() {
        bottomLeft = offset.sub(scale)
        topRight = offset.add(scale)
    }

    fun isInRegion(location: Vec2, bottomLeft: Vec2, topRight: Vec2): Boolean =
        location.x > bottomLeft.x
                && location.x < topRight.x
                && location.y > bottomLeft.y
                && location.y < topRight.y
}
