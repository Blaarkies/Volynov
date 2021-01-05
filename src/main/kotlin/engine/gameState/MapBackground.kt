package engine.gameState

import dI
import display.draw.TextureConfig
import display.draw.TextureEnum
import display.graphic.vertex.BasicShapes
import org.jbox2d.common.Vec2
import utility.Common.vectorUnit

class MapBackground {

    private var scale: Vec2 = vectorUnit
    private var offset: Vec2 = Vec2()
    private var textureConfigNear: TextureConfig
    private var textureConfigFar: TextureConfig

    init {
        scale.mulLocal(10000f)
        val (width, height) = listOf(2f, 1f)
        val tilingFactor = 50f

        textureConfigNear = TextureConfig(TextureEnum.stars_2k_transparent,
            chunkedVertices = BasicShapes.square)
            .also {
                it.gpuBufferData = it.chunkedVertices.flatMap { (x, y) ->
                    listOf(
                        x * width, y * height, 0f,
                        0f, 0f, -1f,
                        it.color.red, it.color.green, it.color.blue, it.color.alpha,
                        (x * .5f - .5f) * tilingFactor,
                        (y * .5f - .5f) * tilingFactor
                    )
                }.toFloatArray()
            }

        textureConfigFar = TextureConfig(TextureEnum.stars_2k, chunkedVertices = BasicShapes.square)
            .also {
                it.gpuBufferData = it.chunkedVertices.flatMap { (x, y) ->
                    listOf(
                        x * width, y * height, 0f,
                        0f, 0f, -1f,
                        it.color.red, it.color.green, it.color.blue, it.color.alpha,
                        (x * .5f - .5f) * tilingFactor,
                        (y * .5f - .5f) * tilingFactor
                    )
                }.toFloatArray()
            }

    }

    fun render() {
        val farBackDropZ = -400f

        dI.textures.getTexture(textureConfigFar.texture).bind()
        dI.renderer.drawShape(textureConfigFar.gpuBufferData, offset, 0f,
            scale, z = farBackDropZ)

        dI.textures.getTexture(textureConfigNear.texture).bind()
        dI.renderer.drawShape(textureConfigNear.gpuBufferData, offset, 2f,
            scale.mul(.973f), z = farBackDropZ * .7f)
    }

}
