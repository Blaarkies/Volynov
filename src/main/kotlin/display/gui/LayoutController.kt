package display.gui

import display.gui.base.GuiElement
import org.jbox2d.common.Vec2
import utility.toSign
import kotlin.math.max
import kotlin.math.min

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
                                   kid: Vec2): Vec2 {
        return when (layoutPosition) {
            LayoutPosition.TOP_LEFT -> kid.sub(parent)
            LayoutPosition.TOP_RIGHT -> kid.sub(parent).also { it.x *= -1f }
            LayoutPosition.BOTTOM_LEFT -> kid.sub(parent).also { it.y *= -1f }
            LayoutPosition.BOTTOM_RIGHT -> parent.sub(kid)
            LayoutPosition.CENTER_LEFT -> Vec2(-(parent.x - kid.x), 0f)
            LayoutPosition.CENTER_RIGHT -> Vec2(parent.x - kid.x, 0f)
        }.also { it.y *= -1f }
    }

    fun getOffsetToFitScaleInside(parentScale: Vec2, parentOffset: Vec2, kidScale: Vec2, kidOffset: Vec2): Vec2 {
        val parentCorners = listOf(
            parentOffset.add(parentScale),
            parentOffset.add(Vec2(-parentScale.x, parentScale.y)),
            parentOffset.sub(parentScale),
            parentOffset.add(Vec2(parentScale.x, -parentScale.y)))

        val kidCorners = listOf(
            kidOffset.add(kidScale),
            kidOffset.add(Vec2(-kidScale.x, kidScale.y)),
            kidOffset.sub(kidScale),
            kidOffset.add(Vec2(kidScale.x, -kidScale.y)))

        val parentBottomLeft = parentCorners[2]
        val parentTopRight = parentCorners[0]
        val kidBottomLeft = kidCorners[2]
        val kidTopRight = kidCorners[0]

        val intersectBottomLeft = Vec2(
            max(kidBottomLeft.x, parentBottomLeft.x),
            max(kidBottomLeft.y, parentBottomLeft.y))

        val intersectTopRight = Vec2(
            min(kidTopRight.x, parentTopRight.x),
            min(kidTopRight.y, parentTopRight.y))

        val intersectCorners: List<Vec2>
        if (intersectBottomLeft.x > intersectTopRight.x
            || intersectBottomLeft.y > intersectTopRight.y
            || intersectBottomLeft.sub(intersectTopRight).length() == 0f) {
            val falseCorner = listOf(intersectBottomLeft, intersectTopRight)
                .maxByOrNull { it.length() }!!
            val validCorner = parentCorners.minByOrNull { it.sub(falseCorner).length() }!!
            intersectCorners = listOf(validCorner)
        } else {
            intersectCorners = listOf(
                intersectTopRight,
                Vec2(intersectBottomLeft.x, intersectTopRight.y),
                intersectBottomLeft,
                Vec2(intersectTopRight.x, intersectBottomLeft.y))
        }

        return kidCorners.zip(intersectCorners)
            .withIndex<Pair<Vec2, Vec2>>()
            .filter { (_, zip) ->
                val (kid, inter) = zip
                kid.sub(inter).length() == 0f
            }
            .let { matches ->
                when (matches.size) {
                    0 -> { // Kid is out of bounds, match via furthest corners
                        val falseCorner = kidCorners.maxByOrNull { it.length() }!!
                        val validCorner = intersectCorners[0]

                        validCorner.sub(falseCorner)
                    }
                    1 -> { // Kid overlaps a parent corner, match via furthest corners
                        val oppositeCornerIndex = matches.first().index.plus(2).rem(4)
                        val falseCorner = kidCorners[oppositeCornerIndex]
                        val validCorner = intersectCorners[oppositeCornerIndex]

                        validCorner.sub(falseCorner)
                    }
                    2 -> { // Kid overlaps with 2 corners, match only 1 axis
                        val cornerAIndex = matches[0].index
                        val cornerBIndex = matches[1].index
                        val falseCornerIndex = (cornerBIndex - cornerAIndex > 1).toSign().toInt()
                        val oppositeCornerIndex = cornerAIndex.plus(falseCornerIndex).rem(4)
                        val falseCorner = kidCorners[oppositeCornerIndex]
                        val validCorner = intersectCorners[oppositeCornerIndex]

                        validCorner.sub(falseCorner)
                    }
                    else -> Vec2()
                }
            }
    }

}
