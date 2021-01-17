package display.graphic.vertex

import org.joml.Vector3f
import utility.padEnd
import utility.subClone

class Triangle(vararg points: Vector3f) {

    constructor(points: List<Vector3f>) : this(points[0], points[1], points[2])

    constructor(isNew: Boolean, vertices: List<Vertex>) : this(
        vertices[0].location,
        vertices[1].location,
        vertices[2].location
    )

    val vertices: List<Vertex> = emptyList()

    val oldVertices: List<Vector3f>
    private val _connectedByVertexTriangles = mutableSetOf<Triangle>()
    private val _connectedByEdgeTriangles = mutableSetOf<Triangle>()

    val connectedByVertexTriangles
        get() = _connectedByVertexTriangles.toList()
    val connectedByEdgeTriangles
        get() = _connectedByEdgeTriangles.toList()

    fun addConnectedTriangle(triangle: Triangle) {
        if (triangle != this) _connectedByVertexTriangles.add(triangle) else Unit
        updateConnectedByEdgeTriangles()
    }

    fun addConnectedTriangles(triangles: MutableSet<Triangle>) {
        _connectedByVertexTriangles.addAll(triangles.filter { it != this })
        updateConnectedByEdgeTriangles()
    }

    private fun updateConnectedByEdgeTriangles() {
        _connectedByEdgeTriangles.clear()
        val trianglesBorderingByEdge = edges.flatMap { (a, b) ->
            _connectedByVertexTriangles.filter { triangle ->
                triangle.oldVertices.count { it == a || it == b } == 2
            }
        }
        _connectedByEdgeTriangles.addAll(trianglesBorderingByEdge)
    }

    /**
     * Normal is calculated to point towards the camera when `vertices` are ordered in an anticlockwise direction
     */
    val normal: Vector3f
        get() = oldVertices[1].subClone(oldVertices[2])
            .cross(oldVertices[0].subClone(oldVertices[1]))
            .normalize()

    init {
        oldVertices = when {
            points.isNotEmpty() -> points.toList()
            else -> BasicShapes.polygon3
                .map { (x, y) -> Vector3f(x, y, 0f) }
        }
    }

    val edges
        get() = oldVertices.padEnd().windowed(2)

}
