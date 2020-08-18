package display.draw

import display.graphic.vertex.Triangle
import utility.component1
import utility.component2
import utility.component3

class Model(val triangles: List<Triangle>, val texture: TextureEnum) {

    lateinit var gpuData: FloatArray

    init {
        updateGpuData()
    }

    private fun updateGpuData() {
        gpuData = triangles.flatMap {
            val vertices = it.vertices
            val (nx, ny, nz) = it.normal

            vertices.flatMap { (x, y, z) ->
                listOf(
                    x, y, z,
                    nx, ny, nz,
                    1f, 1f, 1f, 1f,
                    (x * .5f - .5f),
                    (y * .5f - .5f)
                )
            }
        }.toFloatArray()
    }

}
