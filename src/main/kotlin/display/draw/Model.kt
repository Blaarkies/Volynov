package display.draw

import display.graphic.vertex.Triangle
import utility.*

class Model(var triangles: List<Triangle>, val texture: TextureEnum) {

    lateinit var gpuData: FloatArray

    init {
        updateGpuData()
    }

    fun updateGpuData() {
        gpuData = triangles.flatMap {
            it.vertices.flatMap { (location, texture, normal) ->
                listOf(
                    location.x, location.y, location.z,
                    normal.x, normal.y, normal.z,
                    1f, 1f, 1f, 1f,
                    texture.x, texture.y,
                )
            }
        }.toFloatArray()
    }

    fun clone() = Model(
        triangles.map { it.clone() },
        texture
    )

    companion object {

        fun load(name: String): Model {
            val safePath = Common.getSafePath("/models/$name")
            return WavefrontObject.import(safePath)
        }

    }

}
