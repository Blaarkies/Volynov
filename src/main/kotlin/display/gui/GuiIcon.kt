package display.gui

import display.draw.Drawer
import display.draw.TextureEnum
import display.graphic.BasicShapes
import display.graphic.Color
import display.graphic.SnipRegion
import org.jbox2d.common.Vec2
import utility.Common.vectorUnit

class GuiIcon(
    drawer: Drawer,
    offset: Vec2 = Vec2(),
    scale: Vec2 = vectorUnit,
    title: String = "",
    textSize: Float = 0f,
    color: Color = Color.WHITE.setAlpha(.7f),
    val texture: TextureEnum = TextureEnum.white_pixel
) : GuiElement(drawer, offset, scale, title, textSize, color, {}) {

    override fun render(snipRegion: SnipRegion?) {
        super.render(snipRegion)

        drawer.drawIcon(texture, scale, offset, color)
    }

}
