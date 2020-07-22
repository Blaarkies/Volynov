package display.gui.elements

import dI
import display.draw.TextureConfig
import display.draw.TextureEnum
import display.graphic.BasicShapes
import display.graphic.Color
import display.graphic.SnipRegion
import display.gui.base.GuiElement
import display.gui.base.GuiElementIdentifierType
import display.gui.base.GuiElementPhase
import org.jbox2d.common.Vec2
import utility.Common.vectorUnit

class GuiIcon(
    override val offset: Vec2 = Vec2(),
    override val scale: Vec2 = vectorUnit,
    override var color: Color = Color.WHITE.setAlpha(.7f),
    texture: TextureEnum = TextureEnum.white_pixel,
    val padding: Vec2 = Vec2()
) : GuiElement {

    override var id = GuiElementIdentifierType.DEFAULT
    override var currentPhase = GuiElementPhase.IDLE
    override val updateCallback = { _: GuiElement -> Unit }

    private var outputScale = scale.sub(padding)
    val textureConfig: TextureConfig = TextureConfig(texture,
        chunkedVertices = BasicShapes.square.chunked(2), color = color)
        .updateGpuBufferData()

    override fun render(parentSnipRegion: SnipRegion?) {
        super.render(parentSnipRegion)

        dI.textures.getTexture(textureConfig.texture).bind()
        dI.renderer.drawShape(
            textureConfig.gpuBufferData,
            offset,
            0f,
            outputScale,
            false,
            parentSnipRegion)
    }

    override fun updateScale(newScale: Vec2) {
        super.updateScale(newScale)
        outputScale = scale.sub(padding)
    }

}
