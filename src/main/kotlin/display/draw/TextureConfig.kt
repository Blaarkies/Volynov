package display.draw

import display.graphic.Color
import org.jbox2d.common.Vec2
import utility.Common.vectorUnit

class TextureConfig(
    var texture: TextureEnum,
    val scale: Vec2 = vectorUnit,
    val offset: Vec2 = Vec2(),
    var chunkedVertices: List<List<Float>> = listOf(),
    var gpuBufferData: FloatArray = floatArrayOf(),
    var color: Color = Color.WHITE
) {

    fun updateGpuBufferData(): TextureConfig {
        gpuBufferData = chunkedVertices.flatMap { (x, y) ->
            listOf(
                x, y, 0f,
                color.red, color.green, color.blue, color.alpha,
                (x * .5f - 0.5f) * scale.x + offset.x,
                (y * .5f - 0.5f) * scale.y + offset.y
            )
        }.toFloatArray()
        return this
    }

    fun clone(): TextureConfig = TextureConfig(texture, scale.clone(), offset.clone(),
        chunkedVertices.toList(), color = color)
        .also { it.updateGpuBufferData() }

}


