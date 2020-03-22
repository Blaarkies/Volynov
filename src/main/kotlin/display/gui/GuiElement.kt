package display.gui

import display.draw.Drawer
import display.graphic.Color
import org.jbox2d.common.Vec2

open class GuiElement(
    protected val drawer: Drawer,
    protected val title: String,
    protected val offset: Vec2,
    protected var textSize: Float,
    protected val color: Color
) {
    protected val vectorIdentity = Vec2(1f, 1f)

    protected var currentPhase = GuiElementPhases.IDLE

    init {
        textSize = textSize.coerceAtLeast(.3f)
    }

    open fun render() {
        drawer.renderer.drawText(
            title, offset, vectorIdentity.mul(textSize),
            color, false
        )
    }

}
