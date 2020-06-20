package display.gui

import display.draw.Drawer
import display.draw.TextureEnum
import display.graphic.Color
import display.graphic.SnipRegion
import org.jbox2d.common.Vec2
import utility.Common.vectorUnit

class GuiIcon(
    override val drawer: Drawer,
    override val offset: Vec2 = Vec2(),
    scale: Vec2 = vectorUnit,
    override val color: Color = Color.WHITE.setAlpha(.7f),
    val texture: TextureEnum = TextureEnum.white_pixel
) : GuiElement {

    override val scale: Vec2 = scale.mul(2f)
    override var id = GuiElementIdentifierType.DEFAULT
    override var currentPhase = GuiElementPhases.IDLE
    override val updateCallback = { _: GuiElement -> Unit }

    override fun render(parentSnipRegion: SnipRegion?) {
        super.render(parentSnipRegion)

        drawer.drawIcon(texture, scale, offset, color)
    }

}
