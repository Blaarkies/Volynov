package display.graphic

import display.gui.base.GuiElement
import org.jbox2d.common.Vec2
import utility.Common.makeVec2
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

class SnipRegion(val bottomLeft: Vec2, val topRight: Vec2) {

    val x = bottomLeft.x.toInt()
    val y = bottomLeft.y.toInt()
    val sizeX = topRight.x.minus(bottomLeft.x).absoluteValue.toInt()
    val sizeY = topRight.y.minus(bottomLeft.y).absoluteValue.toInt()

    fun intersect(b: SnipRegion?): SnipRegion? {
        if (b == null) return this

        val intersectBottomLeft = Vec2(
            max(bottomLeft.x, b.bottomLeft.x),
            max(bottomLeft.y, b.bottomLeft.y))

        val intersectTopRight = Vec2(
            min(topRight.x, b.topRight.x),
            min(topRight.y, b.topRight.y))

        if (intersectBottomLeft.x > intersectTopRight.x
            || intersectBottomLeft.y > intersectTopRight.y
            || intersectBottomLeft.sub(intersectTopRight).length() == 0f) {
            val zeroVector = Vec2()
            return SnipRegion(zeroVector, zeroVector)
        }

        return SnipRegion(intersectBottomLeft, intersectTopRight)
    }

    companion object {

        fun create(guiElement: GuiElement): SnipRegion {
            val paddedScale = guiElement.scale.add(makeVec2(1))
            val bottomLeft = guiElement.offset.sub(paddedScale)
            val topRight = guiElement.offset.add(paddedScale)
            return SnipRegion(bottomLeft, topRight)
        }

    }

}
