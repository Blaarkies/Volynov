package display.gui

import org.jbox2d.common.Vec2
import utility.toSign

object LayoutController {

    fun setElementsInColumns(elements: List<GuiElement>, gap: Float = 0f, centered: Boolean = true) {
        setElementsInStacks(elements, gap, centered, true)
    }

    fun setElementsInRows(elements: List<GuiElement>, gap: Float = 0f, centered: Boolean = true) {
        setElementsInStacks(elements, gap, centered, false)
    }

    private fun setElementsInStacks(elements: List<GuiElement>, gap: Float, centered: Boolean, isHorizontal: Boolean) {
        if (elements.isEmpty()) return

        val getMeasure = if (isHorizontal) { vec: Vec2 -> vec.x }
        else { vec: Vec2 -> vec.y }

        val makeOffsetFromMeasure = if (isHorizontal) { offset: Vec2, measure: Float -> Vec2(measure, offset.y) }
        else { offset: Vec2, measure: Float -> Vec2(offset.x, measure) }

        val fixVertical = isHorizontal.toSign()

        val firstElement = elements.first()
        val lastElement = elements.last()

        var sumOffset = getMeasure(firstElement.offset)

        elements.windowed(2)
            .map { (a, b) ->
                val result = Pair(a, sumOffset)
                val movement = gap + getMeasure(a.scale) + getMeasure(b.scale)
                sumOffset += movement.times(fixVertical)
                result
            }
            .let { it + Pair(lastElement, sumOffset) }
            .let {
                when {
                    centered -> {
                        val totalUsedLength = elements.map { element -> getMeasure(element.scale) * 2f }.sum()
                            .plus(gap * (elements.size - 1))
                            .minus(getMeasure(firstElement.scale) + getMeasure(lastElement.scale))
                        val halfTotalLength = totalUsedLength / 2f
                        it.map { (element, offset) ->
                            val centeredOffset = offset - halfTotalLength.times(fixVertical)
                            Pair(element, centeredOffset)
                        }
                    }
                    else -> it
                }
            }
            .forEach { (element, measure) -> element.updateOffset(makeOffsetFromMeasure(element.offset, measure)) }
    }

    fun getOffsetForLayoutPosition(layoutPosition: LayoutPosition,
                                   parent: Vec2,
                                   child: Vec2): Vec2 {
        return when (layoutPosition) {
            LayoutPosition.TOP_LEFT -> child.sub(parent)
            LayoutPosition.TOP_RIGHT -> child.sub(parent).also { it.x *= -1f }
            LayoutPosition.BOTTOM_LEFT -> child.sub(parent).also { it.y *= -1f }
            LayoutPosition.BOTTOM_RIGHT -> parent.sub(child)
        }.also { it.y *= -1f }
    }

}
