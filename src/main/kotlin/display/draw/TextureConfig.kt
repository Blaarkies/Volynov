package display.draw

import Vector2f

class TextureConfig(
    var texture: TextureEnum, val scale: Vector2f = Vector2f(1f, 1f), val offset: Vector2f = Vector2f(),
    var chunkedVertices: List<List<Float>> = listOf(), var gpuBufferData: FloatArray = floatArrayOf()
) {

    fun updateGpuBufferData(): TextureConfig {
        gpuBufferData = chunkedVertices.flatMap {
            val (x, y) = it
            listOf(
                x, y, 0f,
                1f, 1f, 1f, 1f,
                (x * .5f - 0.5f) * scale.x + offset.x,
                (y * .5f - 0.5f) * scale.y + offset.y
            )
        }.toFloatArray()
        return this
    }

}


