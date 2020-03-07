package display.draw

import Vector2f
import display.graphic.Texture

class TextureConfig(
    val texture: Texture, val scale: Vector2f = Vector2f(1f, 1f), val offset: Vector2f = Vector2f(),
    var chunkedVertices: List<List<Float>> = listOf(), var gpuBufferData: FloatArray = floatArrayOf()
) {

    fun updateGpuBufferData() {
        gpuBufferData = chunkedVertices.flatMap {
            listOf(
                it[0], it[1], 0f,
                1f, 1f, 1f, 1f,
                (it[0] / 2 - 0.5f) * scale.x + offset.x,
                (it[1] / 2 - 0.5f) * scale.y + offset.y
            )
        }.toFloatArray()
    }

}


