package display.graphic.vertex

import org.joml.Vector3f
import utility.padEnd

class Triangle(vararg vertices: Vertex) {

    constructor(vertices: List<Vertex>) : this(vertices[0], vertices[1], vertices[2])

    val vertices: List<Vertex> = vertices.toList()
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
                triangle.vertices.count { it == a || it == b } == 2
            }
        }
        _connectedByEdgeTriangles.addAll(trianglesBorderingByEdge)
    }

    /**
     * Normal is calculated to point towards the camera when `vertices` are ordered in an anticlockwise direction
     */
    val normal: Vector3f
        get() = vertices.map { it.normal }
            .fold(Vector3f()) { sum, v -> sum.add(v) }
            .div(3f)
            .normalize()

    val edges
        get() = vertices.padEnd().windowed(2)

    fun clone() = Triangle(vertices.map { it.clone() })

}
