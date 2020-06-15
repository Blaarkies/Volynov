package display.gui

import display.draw.Drawer
import display.graphic.Color
import display.graphic.SnipRegion
import display.text.TextJustify
import org.jbox2d.common.Vec2
import utility.Common.vectorUnit

open class GuiElement(
    protected val drawer: Drawer,
    override var offset: Vec2,
    override val scale: Vec2 = vectorUnit,
    override var title: String = "",
    override val textSize: Float = .15f,
    override val color: Color = Color.WHITE,
    override val updateCallback: (GuiElement) -> Unit = {},
    override var id: GuiElementIdentifierType = GuiElementIdentifierType.DEFAULT
) : GuiElementInterface {

    internal var currentPhase = GuiElementPhases.IDLE

    protected var topRight: Vec2 = Vec2()
    protected var bottomLeft: Vec2 = Vec2()

    override fun render(snipRegion: SnipRegion?) = Unit

    override fun update() = updateCallback(this)

    override fun addOffset(newOffset: Vec2) = addOffset(this, newOffset)

    override fun updateOffset(newOffset: Vec2) = updateOffset(this, newOffset)

    override fun handleHover(location: Vec2) = when {
        isHover(location) -> currentPhase = GuiElementPhases.HOVER
        else -> currentPhase = GuiElementPhases.IDLE
    }

    override fun handleLeftClickPress(location: Vec2): Boolean = false

    override fun handleLeftClickRelease(location: Vec2): Boolean = false

    override fun handleLeftClickDrag(location: Vec2, movement: Vec2): Boolean = false

    override fun handleScroll(location: Vec2, movement: Vec2): Boolean = false

    fun isHover(location: Vec2): Boolean = isInRegion(location, bottomLeft, topRight)

    companion object {

        fun drawLabel(drawer: Drawer,
                      element: GuiElementInterface,
                      justify: TextJustify = TextJustify.CENTER,
                      snipRegion: SnipRegion?
        ) = drawer.renderer.drawText(
            element.title,
            element.offset,
            vectorUnit.mul(element.textSize),
            element.color,
            justify,
            false,
            snipRegion)

        fun calculateElementRegion(element: GuiElement) {
            element.bottomLeft = element.offset.sub(element.scale)
            element.topRight = element.offset.add(element.scale)
        }

        fun addOffset(element: GuiElement, newOffset: Vec2) = updateOffset(element, element.offset.add(newOffset))

        fun updateOffset(element: GuiElement, newOffset: Vec2) {
            element.offset.set(newOffset)
            calculateElementRegion(element)
        }

        fun isInRegion(location: Vec2, regionBottomLeft: Vec2, regionTopRight: Vec2): Boolean =
            location.x > regionBottomLeft.x
                    && location.x < regionTopRight.x
                    && location.y > regionBottomLeft.y
                    && location.y < regionTopRight.y

    }

}
