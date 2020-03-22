package display.gui

import display.draw.Drawer
import display.graphic.Color
import org.jbox2d.common.Vec2

class GuiLabel(
    drawer: Drawer,
    title: String,
    offset: Vec2 = Vec2(),
    textSize: Float = 0f,
    color: Color = Color.WHITE.setAlpha(.7f)
) : GuiElement(drawer, title, offset, textSize, color)
