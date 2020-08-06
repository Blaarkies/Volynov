package display.gui.special

import display.graphic.Color
import display.graphic.Color.Companion.WHITE
import display.graphic.SnipRegion
import display.gui.base.GuiElement
import display.gui.base.GuiElementIdentifierType
import display.gui.base.GuiElementPhase.IDLE
import display.gui.base.HasClick
import display.gui.elements.GuiButton
import display.gui.elements.GuiLabel
import org.jbox2d.common.Vec2
import utility.Common.makeVec2

class GuiSpinner(override val offset: Vec2 = Vec2(),
                 override val scale: Vec2 = Vec2(),
                 override var color: Color = WHITE,
                 override val updateCallback: (GuiElement) -> Unit = {})
    : HasClick {

    private val localElements = mutableListOf<GuiElement>()
    private val localElementOffsets = HashMap<GuiElement, Vec2>()

    override val onClick: () -> Unit = {}
    override lateinit var topRight: Vec2
    override lateinit var bottomLeft: Vec2
    override var id = GuiElementIdentifierType.DEFAULT
    override var currentPhase = IDLE

    init {
        localElements.addAll(listOf(
            GuiButton(Vec2(-50f, 8f), Vec2(11f, 8f), "^", .1f),
            GuiButton(Vec2(-50f, -8f), Vec2(11f, 8f), "v", .1f),
            GuiLabel(Vec2(-30f, 0f), title = "359.99º", textSize = .13f)
        ))
        localElementOffsets.putAll(localElements.map { Pair(it, it.offset.clone()) })

        calculateElementRegion()
    }

    override fun render(parentSnipRegion: SnipRegion?) {
        localElements.forEach { it.render(parentSnipRegion) }

    }

    override fun updateOffset(newOffset: Vec2) {
        super.updateOffset(newOffset)
        localElements.forEach { it.updateOffset(localElementOffsets[it]!!.add(offset)) }
    }

    override fun updateScale(newScale: Vec2) {
        val scaleRatio = Vec2(newScale.x / scale.x, newScale.y / scale.y)
        localElementOffsets.forEach {
            val scale = it.value
            scale.set(scale.x * scaleRatio.x, scale.y * scaleRatio.y)
        }
        super.updateScale(newScale)
    }

}
