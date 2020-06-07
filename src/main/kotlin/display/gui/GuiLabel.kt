package display.gui

import display.draw.Drawer
import display.graphic.Color
import display.graphic.SnipRegion
import display.text.TextJustify
import org.jbox2d.common.Vec2

class GuiLabel(
    drawer: Drawer,
    offset: Vec2 = Vec2(),
    val justify: TextJustify = TextJustify.LEFT,
    title: String,
    textSize: Float = 0f,
    color: Color = Color.WHITE.setAlpha(.7f),
    updateCallback: (GuiElement) -> Unit = {}
) : GuiElement(
    drawer,
    offset,
    Vec2(title.length * textSize * 60f, textSize * 100f),
    title,
    textSize,
    color,
    updateCallback
) {

    override fun render(snipRegion: SnipRegion?) {
        super.render(snipRegion)
        drawLabel(drawer, this, justify, snipRegion)
    }

}
