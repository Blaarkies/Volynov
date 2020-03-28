package display.gui

import display.draw.Drawer
import display.graphic.Color
import org.jbox2d.common.Vec2
import utility.Common

open class GuiElement(
    protected val drawer: Drawer,
    override var offset: Vec2,
    override val scale: Vec2 = Vec2(1f, 1f),
    override val title: String,
    override val textSize: Float,
    override val color: Color,
    override var id: GuiElementIdentifierType = GuiElementIdentifierType.DEFAULT
) : GuiElementInterface {

    protected var currentPhase = GuiElementPhases.IDLE

    protected var topRight: Vec2 = Vec2()
    protected var bottomLeft: Vec2 = Vec2()

    override fun render() {
    }

    override fun addOffset(newOffset: Vec2) {
        GuiElement.addOffset(this, newOffset)
    }

    override fun updateOffset(newOffset: Vec2) {
        GuiElement.updateOffset(this, newOffset)
    }

    override fun handleHover(location: Vec2) {
        when {
            isHover(location) -> currentPhase = GuiElementPhases.HOVERED
            else -> currentPhase = GuiElementPhases.IDLE
        }
    }

    override fun handleClick(location: Vec2) {
    }

    protected fun isHover(location: Vec2): Boolean {
        return location.x > bottomLeft.x
                && location.x < topRight.x
                && location.y > bottomLeft.y
                && location.y < topRight.y
    }

    companion object {

        fun drawLabel(drawer: Drawer, element: GuiElementInterface) {
            drawer.renderer.drawText(
                element.title, element.offset,
                Common.vectorUnit.mul(element.textSize),
                element.color, false
            )
        }

        fun calculateElementRegion(element: GuiElement) {
            element.bottomLeft = element.offset.add(element.scale.negate())
            element.topRight = element.offset.add(element.scale)
        }

        fun addOffset(element: GuiElement, newOffset: Vec2) {
            updateOffset(element, element.offset.add(newOffset))
        }

        fun updateOffset(element: GuiElement, newOffset: Vec2) {
            element.offset = newOffset
            calculateElementRegion(element)
        }

    }

}
