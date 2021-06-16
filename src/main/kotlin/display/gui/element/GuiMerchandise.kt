package display.gui.element

import dI
import display.draw.TextureEnum
import display.event.MouseButtonEvent
import display.graphic.Color
import display.graphic.SnipRegion
import display.gui.LayoutPosition
import display.gui.base.GuiElementPhase.*
import display.gui.base.*
import display.text.TextJustify
import io.reactivex.Observable
import org.jbox2d.common.Vec2
import utility.Common.muCron
import utility.Common.makeVec2

class GuiMerchandise(
    override val offset: Vec2 = Vec2(),
    override val scale: Vec2 = Vec2(200f, 50f),
    var name: String = "",
    val price: Int,
    val itemId: String,
    val description: String,
    val key: String = "",
    override var color: Color = Color.WHITE.setAlpha(.7f),
    override val onClick: () -> Unit = {},
    override val updateCallback: (GuiElement) -> Unit = {}
) : HasClick, HasOutline {

    override lateinit var outline: FloatArray
    override lateinit var activeBackground: FloatArray
    override var backgroundColor = color.setAlpha(.1f)

    override lateinit var topRight: Vec2
    override lateinit var bottomLeft: Vec2
    override var id = GuiElementIdentifierType.DEFAULT
    override var currentPhase = IDLE

    private val localElements = mutableListOf<GuiElement>()
    private val localElementOffsets = HashMap<GuiElement, Vec2>()

    init {
        localElements.addAll(listOf(
            GuiIcon(Vec2(), scale, Color.WHITE.setAlpha(.4f), TextureEnum.danger, Vec2(2f, 4f))
                .also { it.textureConfig.updateGpuBufferDataWithTilingFactor(Vec2(8f, 1f)) },
            GuiIcon(Vec2(scale.x - scale.y, 0f), makeVec2(scale.y), texture = TextureEnum.icon_question_circle,
                padding = makeVec2(10f))
        ))
        localElementOffsets.putAll(localElements.map { Pair(it, it.offset.clone()) })

        calculateVisuals()
        calculateElementRegion()
    }

    override fun calculateVisuals() {
        super.calculateVisuals()
        localElements[0].updateScale(scale)
        localElements[1].updateScale(makeVec2(scale.y))
        localElements[1].placeOnEdge(LayoutPosition.CENTER_RIGHT, scale)
    }

    override fun render(parentSnipRegion: SnipRegion?) {
        dI.oldRenderer.drawText(name, offset.sub(Vec2(scale.x - 4f, 0f)), makeVec2(.13),
            color, useCamera = false, snipRegion = parentSnipRegion)

        val tooltipIcon = localElements[1]
        tooltipIcon.render(parentSnipRegion)

        dI.oldRenderer.drawText("$price$muCron",
            offset.add(Vec2(scale.x - tooltipIcon.scale.x * 2, 0f)), makeVec2(.13),
            color, justify = TextJustify.RIGHT, useCamera = false, snipRegion = parentSnipRegion)

        dI.textures.getTexture(TextureEnum.white_pixel).bind()
        when (currentPhase) {
            HOVER, ACTIVE ->
                dI.oldRenderer.drawShape(activeBackground, offset, useCamera = false, snipRegion = parentSnipRegion)
            DISABLED -> localElements[0].render(parentSnipRegion)
            else -> Unit
        }

        when (currentPhase) {
            ACTIVE -> {
                dI.oldRenderer.drawStrip(outline, offset,
                    scale = Vec2((scale.x - 2f) / scale.x, (scale.y - 2f) / scale.y),
                    useCamera = false, snipRegion = parentSnipRegion)
            }
            else -> super<HasOutline>.render(parentSnipRegion)
        }

        super<HasClick>.render(parentSnipRegion)
    }

    override fun updateOffset(newOffset: Vec2) {
        super<HasOutline>.updateOffset(newOffset)
        super<HasClick>.updateOffset(newOffset)
        localElements.forEach { it.updateOffset(localElementOffsets[it]!!.add(offset)) }
    }

    override fun updateScale(newScale: Vec2) {
        val scaleRatio = Vec2(newScale.x / scale.x, newScale.y / scale.y)
        localElementOffsets.forEach {
            val scale = it.value
            scale.set(scale.x * scaleRatio.x, scale.y * scaleRatio.y)
        }
        super<HasOutline>.updateScale(newScale).also { calculateVisuals() }
    }

    override fun handleLeftClick(startEvent: MouseButtonEvent, event: Observable<MouseButtonEvent>): Boolean {
        if (startEvent.location.sub(localElements[1].offset).length() < localElements[1].scale.x * .6f) {
            dI.guiController.tooltip.showElement(
                GuiLabel(offset = Vec2(-180f, 10f), title = description, textSize = 0.11f, maxWidth = 200f),
                offset.add(Vec2(scale.x, scale.y * 2)))
            return true
        }

        return super.handleLeftClick(startEvent, event)
    }

}
