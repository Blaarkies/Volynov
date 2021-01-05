package display.graphic.vertex

import org.joml.Vector3f
import utility.padEnd
import utility.subClone

class Triangle(vararg points: Vector3f) {

    val vertices: List<Vector3f>
    val normal: Vector3f
        get() = vertices[1].subClone(vertices[2])
            .cross(vertices[0].subClone(vertices[1]))
            .normalize()

    init {
        vertices = when {
            points.isNotEmpty() -> points.toList()
            else -> BasicShapes.polygon3
                .map { (x, y) -> Vector3f(x, y, 0f) }
        }
    }

    val sides
        get() = vertices.padEnd().windowed(2)

}
