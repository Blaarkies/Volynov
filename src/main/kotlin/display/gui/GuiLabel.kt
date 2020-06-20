package display.gui

import display.draw.Drawer
import display.graphic.Color
import display.text.TextJustify
import org.jbox2d.common.Vec2

class GuiLabel(
    override val drawer: Drawer,
    override val offset: Vec2 = Vec2(),
    override val justify: TextJustify = TextJustify.LEFT,
    override var title: String,
    override val textSize: Float = 0f,
    override val color: Color = Color.WHITE.setAlpha(.7f),
    override val updateCallback: (GuiElement) -> Unit = {}
) : HasLabel {

    override val scale = drawer.renderer.font.getScale(title, textSize)
    override var id = GuiElementIdentifierType.DEFAULT
    override var currentPhase = GuiElementPhases.IDLE

}
