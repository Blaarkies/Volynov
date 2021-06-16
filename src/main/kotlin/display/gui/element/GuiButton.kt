package display.gui.element

import dI
import display.draw.TextureEnum
import display.graphic.Color
import display.graphic.SnipRegion
import display.gui.base.*
import display.gui.base.GuiElementPhase.*
import display.text.TextJustify
import org.jbox2d.common.Vec2
import utility.Common.makeVec2

open class GuiButton(
    override val offset: Vec2 = Vec2(),
    override val scale: Vec2 = Vec2(200f, 50f),
    override var title: String = "",
    override val textSize: Float = .2f,
    val icon: TextureEnum? = null,
    override val angle: Float = 0f,
    override var color: Color = Color.WHITE.setAlpha(.7f),
    override val onClick: () -> Unit = {},
    override val updateCallback: (GuiElement) -> Unit = {}
) : HasClick, HasOutline, HasLabel {

    override var maxWidth = scale.x * 2
    override lateinit var outline: FloatArray
    override lateinit var activeBackground: FloatArray
    override var backgroundColor = color.setAlpha(.1f)

    override val justify = TextJustify.CENTER
    override lateinit var topRight: Vec2
    override lateinit var bottomLeft: Vec2
    override var id = GuiElementIdentifierType.DEFAULT
    override var currentPhase = IDLE

    private var guiIcon: GuiIcon? = null

    init {
        if (icon != null) {
            guiIcon = GuiIcon(offset, scale, angle = angle, texture = icon, padding = makeVec2(1))
        }
        calculateVisuals()
        calculateElementRegion()
    }

    override fun render(parentSnipRegion: SnipRegion?) {
        dI.textures.getTexture(TextureEnum.white_pixel).bind()

        if (currentPhase == HOVER || currentPhase == ACTIVE) {
            dI.oldRenderer.drawShape(activeBackground, offset, useCamera = false, snipRegion = parentSnipRegion)
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
        super<HasLabel>.render(parentSnipRegion)

        if (icon != null) {
            guiIcon!!.render(parentSnipRegion)
        }
    }

    override fun updateScale(newScale: Vec2) = super<HasOutline>.updateScale(newScale).also { calculateVisuals() }

}
