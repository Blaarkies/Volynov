package display.graphic.vertex

import org.joml.Vector3f
import utility.padEnd

class Triangle(vararg points: Vector3f) {

    val vertices: List<Vector3f>

    init {
        vertices = when {
            points.isNotEmpty() -> points.toList()
            else -> BasicShapes.polygon3.chunked(2)
                .map { (x, y) -> Vector3f(x, y, 0f) }
        }
    }

    val sides
        get() = vertices.padEnd().windowed(2)

}
