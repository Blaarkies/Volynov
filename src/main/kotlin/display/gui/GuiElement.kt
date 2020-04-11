package display.gui

import display.draw.Drawer
import display.graphic.Color
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

    override fun render() = Unit

    override fun update() = updateCallback(this)

    override fun addOffset(newOffset: Vec2) = GuiElement.addOffset(this, newOffset)

    override fun updateOffset(newOffset: Vec2) = GuiElement.updateOffset(this, newOffset)

    override fun handleHover(location: Vec2) = when {
        isHover(location) -> currentPhase = GuiElementPhases.HOVERED
        else -> currentPhase = GuiElementPhases.IDLE
    }

    override fun handleClick(location: Vec2) = Unit

    protected fun isHover(location: Vec2): Boolean =
        location.x > bottomLeft.x
            && location.x < topRight.x
            && location.y > bottomLeft.y
            && location.y < topRight.y

    companion object {

        fun drawLabel(drawer: Drawer, element: GuiElementInterface) {
            drawer.renderer.drawText(
                element.title, element.offset,
                vectorUnit.mul(element.textSize),
                element.color, TextJustify.CENTER, false
            )
        }

        fun calculateElementRegion(element: GuiElement) {
            element.bottomLeft = element.offset.add(element.scale.negate())
            element.topRight = element.offset.add(element.scale)
        }

        fun addOffset(element: GuiElement, newOffset: Vec2) = updateOffset(element, element.offset.add(newOffset))

        fun updateOffset(element: GuiElement, newOffset: Vec2) {
            element.offset = newOffset
            calculateElementRegion(element)
        }

    }

}
