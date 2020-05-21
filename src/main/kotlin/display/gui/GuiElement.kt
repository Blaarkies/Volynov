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
    override var title: String,
    override val textSize: Float,
    override val color: Color,
    override val updateCallback: (GuiElement) -> Unit,
    override var id: GuiElementIdentifierType = GuiElementIdentifierType.DEFAULT
) : GuiElementInterface {

    protected var currentPhase = GuiElementPhases.IDLE

    protected var topRight: Vec2 = Vec2()
    protected var bottomLeft: Vec2 = Vec2()

    override fun render(snipRegion: SnipRegion?) = Unit

    override fun update() = updateCallback(this)

    override fun addOffset(newOffset: Vec2) = addOffset(this, newOffset)

    override fun updateOffset(newOffset: Vec2) = updateOffset(this, newOffset)

    override fun handleHover(location: Vec2) = when {
        isHover(location) -> currentPhase = GuiElementPhases.HOVERED
        else -> currentPhase = GuiElementPhases.IDLE
    }

    override fun handleLeftClick(location: Vec2) = Unit

    override fun handleLeftClickDrag(location: Vec2, movement: Vec2) = Unit

    override fun handleScroll(location: Vec2, movement: Vec2) = Unit

    fun isHover(location: Vec2): Boolean =
        location.x > bottomLeft.x
                && location.x < topRight.x
                && location.y > bottomLeft.y
                && location.y < topRight.y

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

    }

}
