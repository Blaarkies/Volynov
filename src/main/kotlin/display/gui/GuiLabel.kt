package display.gui

import display.draw.Drawer
import display.graphic.Color
import org.jbox2d.common.Vec2

class GuiLabel(
    drawer: Drawer,
    offset: Vec2 = Vec2(),
    title: String,
    textSize: Float = 0f,
    color: Color = Color.WHITE.setAlpha(.7f),
    updateCallback: (GuiElement) -> Unit = {}
) : GuiElement(drawer, offset, title = title, textSize = textSize, color = color, updateCallback = updateCallback) {

    override fun render() {
        super.render()

        GuiElement.drawLabel(drawer, this)
    }

}
