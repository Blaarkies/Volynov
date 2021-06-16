package display.graphic.vertex

import org.joml.Vector2f
import org.joml.Vector3f
import utility.clone
import utility.lerpClone

data class Vertex(
    val location: Vector3f = Vector3f(0f, 0f, 0f),
    val texture: Vector2f = Vector2f(0f, 0f),
    val normal: Vector3f = location.clone().normalize(),
    var textureDepth: Float = 0f,
) {

    constructor(vararg locations: Float) : this(Vector3f(locations))

    var connectedVertices: List<Vertex> = emptyList()
    var connectedFaces: List<Triangle> = emptyList()

    fun clone() = Vertex(location.clone(), texture.clone(), normal.clone(), textureDepth)

    fun lerpClone(other: Vertex, t: Float): Vertex = Vertex(
        location.lerpClone(other.location, t),
        texture.lerpClone(other.texture, t),
        normal.lerpClone(other.normal, t),
        textureDepth,
        )

}
