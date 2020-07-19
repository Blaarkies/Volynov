package display.gui.elements

import dI
import display.draw.TextureEnum
import display.graphic.Color
import display.graphic.SnipRegion
import display.gui.LayoutPosition
import display.gui.base.GuiElement
import display.gui.base.GuiElementIdentifierType
import display.gui.base.GuiElementPhase
import display.gui.base.HasOutline
import org.jbox2d.common.Vec2
import utility.Common
import utility.PidController
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin

class GuiProgressBar(override val offset: Vec2 = Vec2(),
                     override val scale: Vec2 = Common.vectorUnit,
                     val angle: Float = 0f,
                     override var color: Color = Color.WHITE.setAlpha(.5f),
                     override val updateCallback: (GuiElement) -> Unit = {}
) : HasOutline, GuiElement {

    override var id = GuiElementIdentifierType.DEFAULT
    override var currentPhase = GuiElementPhase.IDLE
    override lateinit var outline: FloatArray
    override lateinit var activeBackground: FloatArray
    override var backgroundColor = color

    private var progress = 0f // range [0.0f, 1.0f]
    var progressTarget = progress
    private val progressController = PidController(-.05f, -.0001f, -.03f)

    init {
        calculateVisuals()
    }

    override fun update() {
        super<HasOutline>.update()
        if (progress.minus(progressTarget).absoluteValue > .005f) {
            val movement = progressController.getReaction(progress, progressTarget)
            progress = (progress + movement).coerceIn(0f, 1f)
        }
    }

    override fun render(parentSnipRegion: SnipRegion?) {
        dI.textures.getTexture(TextureEnum.white_pixel).bind()
        dI.renderer.drawStrip(outline, offset, angle, useCamera = false, snipRegion = parentSnipRegion)

        if (progress <= .005f) return
        val progressOffset = Vec2(-scale.x + scale.x * progress, 0f)
            .also { rotateVecByAngle(it, angle) }
            .add(offset)

        val progressScale = Vec2(progress, 1f)
        dI.renderer.drawShape(activeBackground, progressOffset, angle, progressScale, useCamera = false,
            snipRegion = parentSnipRegion)
    }

    override fun placeOnEdge(edge: LayoutPosition, parent: Vec2, adjustedScale: Vec2?) {
        val rotatedScale = scale.clone().let {
            rotateVecByAngle(it, angle)
            it.abs()
        }
        super<HasOutline>.placeOnEdge(edge, parent, rotatedScale)
    }

    private fun rotateVecByAngle(vector: Vec2, angle: Float) {
        vector.set(vector.x * cos(angle) - vector.y * sin(angle),
            vector.x * sin(angle) + vector.y * cos(angle))
    }

}
