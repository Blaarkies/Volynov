package display.gui.elements

import dI
import display.graphic.Color
import display.gui.base.GuiElement
import display.gui.base.GuiElementIdentifierType
import display.gui.base.GuiElementPhases
import display.gui.base.HasLabel
import display.text.TextJustify
import org.jbox2d.common.Vec2

class GuiLabel(
    override val offset: Vec2 = Vec2(),
    override val justify: TextJustify = TextJustify.LEFT,
    override var title: String = "",
    override val textSize: Float = 0f,
    override val color: Color = Color.WHITE.setAlpha(.7f),
    override val updateCallback: (GuiElement) -> Unit = {}
) : HasLabel {

    override val scale = dI.renderer.font.getScale(title, textSize)
    override var id = GuiElementIdentifierType.DEFAULT
    override var currentPhase = GuiElementPhases.IDLE

}
