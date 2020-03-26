package display.gui

import display.draw.Drawer
import display.graphic.Color
import org.jbox2d.common.Vec2
import utility.Common

open class GuiElement(
    protected val drawer: Drawer,
    override val offset: Vec2,
    override val scale: Vec2 = Vec2(1f, 1f),
    override val title: String,
    override val textSize: Float,
    override val color: Color,
    override var id: GuiElementIdentifierType = GuiElementIdentifierType.DEFAULT
) : GuiElementInterface {

    protected var currentPhase = GuiElementPhases.IDLE

    protected lateinit var topRight: Vec2
    protected lateinit var bottomLeft: Vec2

    override fun render() {
    }

    fun setOffset(newOffset: Vec2) {
        GuiElement.setOffset(this, newOffset)
    }

    fun handleHover(location: Vec2) {
        when {
            isHover(location) -> currentPhase = GuiElementPhases.HOVERED
            else -> currentPhase = GuiElementPhases.IDLE
        }
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

        fun setOffset(element: GuiElement, newOffset: Vec2) {
            element.offset.addLocal(newOffset)
            calculateElementRegion(element)
        }

    }

}
