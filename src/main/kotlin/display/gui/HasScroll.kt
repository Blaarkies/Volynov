package display.gui

import org.jbox2d.common.Vec2
import utility.PidController
import kotlin.math.absoluteValue

interface HasScroll : HasClick {

    val scrollController: PidController
    var scrollBarPosition: Float
    var scrollBarPositionTarget: Float
    var scrollBarMax: Float
    var scrollBarMin: Float

    override fun update() {
        if (scrollBarPosition.minus(scrollBarPositionTarget).absoluteValue > .1f) {
            val movement = scrollController.getReaction(scrollBarPosition, scrollBarPositionTarget)
            scrollBarPosition = (scrollBarPosition + movement).coerceIn(scrollBarMin, scrollBarMax)
            scrollBarOnMove()
        }
        super.update()
    }

    fun scrollBarOnMove() = Unit

    fun handleScroll(location: Vec2, movement: Vec2): Boolean {
        return when {
            isHover(location) -> {
                addScrollBarPosition(movement.y * scale.y * 1.8f)
                true
            }
            else -> false
        }
    }

    fun addScrollBarPosition(movement: Float) {
        scrollBarPositionTarget = (scrollBarPositionTarget + movement).coerceIn(scrollBarMin, scrollBarMax)
    }

    fun updateScrollBarRange() = Unit

    fun handleLeftClickDrag(location: Vec2, movement: Vec2): Boolean {
        return if (isPressed && isHover(location)) {
            addScrollBarPosition(-movement.y)
            true
        } else false
    }

}
