package display.gui.elements

import display.graphic.Color
import display.gui.base.GuiElement
import display.gui.base.GuiElementIdentifierType
import display.gui.base.GuiElementPhase
import display.gui.base.HasLabel
import display.text.TextJustify
import org.jbox2d.common.Vec2

class GuiLabel(
    override val offset: Vec2 = Vec2(),
    override val justify: TextJustify = TextJustify.LEFT,
    override var title: String = "",
    override val textSize: Float = .1f,
    override var color: Color = Color.WHITE.setAlpha(.7f),
    override val updateCallback: (GuiElement) -> Unit = {},
    override var maxWidth: Float = 500f
) : HasLabel {

    fun updateManual(callback: (GuiLabel) -> Unit) = callback(this)

    override val scale = Vec2(maxWidth * 1.1f, title.length * 900 * textSize / maxWidth)
    override var id = GuiElementIdentifierType.DEFAULT
    override var currentPhase = GuiElementPhase.IDLE

}
