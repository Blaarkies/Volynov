package display.gui.special

import display.graphic.Color
import display.graphic.Color.Companion.WHITE
import display.graphic.SnipRegion
import display.gui.base.GuiElement
import display.gui.base.GuiElementIdentifierType
import display.gui.base.GuiElementPhase.IDLE
import display.gui.base.HasElements
import display.gui.base.HasLabel
import display.gui.elements.GuiButton
import display.gui.elements.GuiLabel
import display.text.TextJustify
import org.jbox2d.common.Vec2
import utility.Common.roundFloat

class GuiSpinner(override val offset: Vec2 = Vec2(),
                 override val scale: Vec2 = Vec2(61f, 16f),
                 override var color: Color = WHITE,
                 override val updateCallback: (GuiElement) -> Unit = {},
                 onClickMore: () -> Unit = {},
                 onClickLess: () -> Unit = {},
                 labelCallback: (GuiElement) -> Unit = {})
    : HasElements {

    override val localElements = mutableListOf<GuiElement>()
    override val localElementOffsets = HashMap<GuiElement, Vec2>()

    override lateinit var topRight: Vec2
    override lateinit var bottomLeft: Vec2
    override var id = GuiElementIdentifierType.DEFAULT
    override var currentPhase = IDLE

    init {
        localElements.addAll(listOf(
            GuiButtonRepeater(Vec2(-50f, 8f), Vec2(11f, 8f), "^", .1f, onClick = onClickMore),
            GuiButtonRepeater(Vec2(-50f, -8f), Vec2(11f, 8f), "v", .1f, onClick = onClickLess),
            GuiLabel(Vec2(50f, 0f), TextJustify.RIGHT, textSize = .14f, updateCallback = labelCallback)
        ))
        localElementOffsets.putAll(localElements.map { Pair(it, it.offset.clone()) })

        calculateElementRegion()
    }

}
