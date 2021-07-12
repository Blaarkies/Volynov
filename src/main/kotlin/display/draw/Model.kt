package display.draw

import display.graphic.CameraType
import display.graphic.vertex.Triangle
import utility.*

class Model(var triangles: List<Triangle>,
            val textures: List<TextureEnum>,
            val scale: Float,
            val cameraType: CameraType) {

    lateinit var gpuData: FloatArray

    init {
        updateGpuData()
    }

    fun updateGpuData() {
        gpuData = triangles.flatMap {
            it.vertices.flatMap { (location, texture, normal, textureDepth) ->
                listOf(
                    location.x, location.y, location.z,
                    texture.x, texture.y,
                    normal.x, normal.y, normal.z, // hard-normals = it.normal.x, it.normal.y, it.normal.z,
                    1f, 1f, 1f, 1f,
                    textureDepth,
                )
            }
        }.toFloatArray()
    }

    fun clone() = Model(
        triangles.map { it.clone() },
        textures,
        scale,
        cameraType,
    )

    companion object {

        fun load(name: String): Model {
            val safePath = Common.getSafePath("/models/$name")
            return WavefrontObject.import(safePath)
        }

    }

}
