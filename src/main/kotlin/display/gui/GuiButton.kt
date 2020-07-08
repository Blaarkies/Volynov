package display.gui

import dI
import display.draw.TextureEnum
import display.graphic.Color
import display.graphic.SnipRegion
import display.text.TextJustify
import org.jbox2d.common.Vec2

class GuiButton(
    override val offset: Vec2 = Vec2(),
    override val scale: Vec2 = Vec2(200f, 50f),
    override var title: String = "",
    override val textSize: Float = .2f,
    override val color: Color = Color.WHITE.setAlpha(.7f),
    override val onClick: () -> Unit = {},
    override val updateCallback: (GuiElement) -> Unit = {}
) : HasClick, HasOutline, HasLabel {

    override lateinit var outline: FloatArray
    override lateinit var activeBackground: FloatArray
    override var backgroundColor = color.setAlpha(.1f)

    override val justify = TextJustify.CENTER
    override lateinit var topRight: Vec2
    override lateinit var bottomLeft: Vec2
    override var id = GuiElementIdentifierType.DEFAULT
    override var currentPhase = GuiElementPhases.IDLE

    init {
        calculateVisuals()
        calculateElementRegion()
    }

    override fun render(parentSnipRegion: SnipRegion?) {
        dI.textures.getTexture(TextureEnum.white_pixel).bind()

        if (currentPhase == GuiElementPhases.HOVER || currentPhase == GuiElementPhases.ACTIVE) {
            dI.renderer.drawShape(activeBackground, offset, useCamera = false, snipRegion = parentSnipRegion)
        }

        when (currentPhase) {
            GuiElementPhases.ACTIVE -> {
                dI.renderer.drawStrip(outline, offset,
                    scale = Vec2((scale.x - 2f) / scale.x, (scale.y - 2f) / scale.y),
                    useCamera = false, snipRegion = parentSnipRegion)
            }
            else -> super<HasOutline>.render(parentSnipRegion)
        }

        super<HasClick>.render(parentSnipRegion)
        super<HasLabel>.render(parentSnipRegion)
    }

    override fun updateScale(newScale: Vec2): Vec2 =
        super<HasOutline>.updateScale(newScale)
            .also { calculateVisuals() }

}
