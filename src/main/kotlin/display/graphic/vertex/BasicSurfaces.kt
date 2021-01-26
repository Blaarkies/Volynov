package display.graphic.vertex

import org.joml.Vector2f
import org.joml.Vector3f
import utility.Common.Pi
import utility.Common.Pi2
import utility.Common.PiH
import utility.clone
import utility.padEnd
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.MutableSet
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.flatMap
import kotlin.collections.flatten
import kotlin.collections.forEach
import kotlin.collections.listOf
import kotlin.collections.map
import kotlin.collections.mutableSetOf
import kotlin.collections.plus
import kotlin.collections.set
import kotlin.collections.windowed
import kotlin.collections.zip
import kotlin.math.atan

object BasicSurfaces {

    /**
     * @see <a href="https://en.wikipedia.org/wiki/Regular_icosahedron">Regular icosahedron - Spherical coordinates</a>
     * <i>The locations of the vertices of a regular icosahedron can be described using spherical coordinates,
     * for instance as latitude and longitude. If two vertices are taken to be at the north and
     * south poles (latitude ±90°), then the other ten vertices are at latitude ±arctan(.5) ≈ ±26.57°.
     * These ten vertices are at evenly spaced longitudes (36° apart), alternating between north and south latitudes.</i>
     */
    val icosahedron: List<Triangle>
        get() {
            val topLocation = Vector3f(0f, 0f, 1f)
            // Upper 5 vertices are at 26.57°, measured from the x-axis towards z-axis.
            // Rotate the topVertex around y-axis, by the amount 90°compliment of arctan(.5)
            val rightLocation = topLocation.clone().rotateY(PiH - atan(.5f))
            // Create 5 vertices, rotated 36° from each other
            val topPentagonLocations = (1..4).map { rightLocation.clone().rotateZ(Pi2 * .2f * it) }
                .let { listOf(rightLocation) + it }
            val bottomPentagonLocations = topPentagonLocations.map {
                // Bottom 5 vertices are rotated, they each line up with the center of an edge from the top 5 vertices
                it.clone()
                    .rotateZ(Pi2 * .1f)
                    .also { copy -> copy.z *= -1f }
            }
            val bottomLocation = topLocation.clone().also { it.z *= -1f }

            val topVertex = Vertex(topLocation)
            val topPentagonVertices = topPentagonLocations.map { Vertex(it) }
            val bottomPentagonVertices = bottomPentagonLocations.map { Vertex(it) }
            val bottomVertexVertex = Vertex(bottomLocation)

            // Shape has 4 "layers" of vertices (the single top vertex, the 5 upper vertices,
            // the lower 5 vertices, and the bottom vertex). This needs 3 "layers" of faces
            val topTriangles = topPentagonVertices.padEnd().windowed(2)
                .map { (a, b) -> Triangle(a, b, topVertex) }
            val bottomTriangles = bottomPentagonVertices.padEnd().windowed(2)
                // Normals are calculated on vertex sequence. Bottom faces need to be reversed
                .map { (a, b) -> Triangle(b, a, bottomVertexVertex) }

            val beltTriangles = topPentagonVertices.padEnd().windowed(2)
                .zip(bottomPentagonVertices.padEnd().windowed(2))
                .flatMap { (top, bottom) ->
                    val topA = top[0]
                    val topB = top[1]
                    val bottomA = bottom[0]
                    val bottomB = bottom[1]

                    listOf(
                        Triangle(topA, bottomA, topB),
                        Triangle(bottomA, bottomB, topB)
                    )
                }

            val triangles = listOf(topTriangles, bottomTriangles, beltTriangles).flatten()
                .also { triangles ->
                    setConnectingTriangles(triangles)
                    setVertexTexture(triangles.flatMap { it.vertices }, MapProjectionType.Mercator)
                    setVertexNormalFromLocation(triangles.flatMap { it.vertices })
                }
            return triangles
        }

    fun setVertexTexture(vertices: List<Vertex>, uvMappingType: MapProjectionType) {
        when (uvMappingType) {
            MapProjectionType.Mercator -> {
                val (bottom, top) = vertices
                    .sortedBy { it.location.z }
                    .let { Pair(it.first().location.z, it.last().location.z) }
                val rangeZ = top - bottom
                val originVector = Vector2f(1f, 0f)

                vertices.forEach { vertex ->
                    vertex.texture.set(
                        Vector2f(vertex.location.x, vertex.location.y)
                            .angle(originVector)
                            .div(Pi2),
                        vertex.location.z.minus(bottom).div(rangeZ)
                    )
                }
            }
        }
    }

    private fun setVertexNormalFromLocation(vertices: List<Vertex>) {
        vertices.forEach { it.normal.set(it.location) }
    }

    private fun setConnectingTriangles(triangles: List<Triangle>): List<Triangle> {
        val vertexTriangleMap = HashMap<Vertex, MutableSet<Triangle>>()
        triangles.forEach { triangle ->
            triangle.vertices.forEach { vertex ->
                when (val mapValue = vertexTriangleMap[vertex]) {
                    null -> vertexTriangleMap[vertex] = mutableSetOf(triangle)
                    else -> mapValue.add(triangle)
                }
            }
        }

        vertexTriangleMap.entries.forEach { (_, triangles) ->
            triangles.forEach { it.addConnectedTriangles(triangles) }
        }

        return triangles
    }

    fun subdivide(triangles: List<Triangle>, depth: Int = 1): List<Triangle> {
        var newTriangles = triangles
        repeat(depth) { newTriangles = subdivideShape(newTriangles) }
        return newTriangles
    }

    private fun subdivideShape(triangles: List<Triangle>): List<Triangle> {
        val edgeToNewVertexMap = HashMap<Pair<Vertex, Vertex>, Vertex>()
        val referenceVertex = triangles[0].vertices[0]
        val referenceVertexClone = referenceVertex.clone()

        val subdividedTriangles = triangles.flatMap { oldTriangle ->
            val newVertices = oldTriangle.edges.map { (a, b) ->
                val key = Pair(a, b)
                val yek = Pair(b, a) // Also check key in reverse to avoid duplicating edges
                when (val existingVertex = edgeToNewVertexMap[key] ?: edgeToNewVertexMap[yek]) {
                    null -> {
                        val newVertex = a.lerpClone(b, .5f)
                        edgeToNewVertexMap[key] = newVertex
                        newVertex
                    }
                    else -> existingVertex
                }
            }

            val topVertex = oldTriangle.vertices[0]
            val leftVertex = oldTriangle.vertices[1]
            val rightVertex = oldTriangle.vertices[2]

            val innerTriangle = Triangle(newVertices)
            val innerLeftVertex = newVertices[0]
            val innerBottomVertex = newVertices[1]
            val innerRightVertex = newVertices[2]

            val topTriangle = Triangle(innerLeftVertex, innerRightVertex, topVertex)
            val leftTriangle = Triangle(leftVertex, innerBottomVertex, innerLeftVertex)
            val rightTriangle = Triangle(innerBottomVertex, rightVertex, innerRightVertex)

            listOf(topTriangle, leftTriangle, rightTriangle, innerTriangle)
        }.let { setConnectingTriangles(it) }

        // New vertices are place inline with existing faces, the shape still renders the same
        // Move the corner vertices of the old triangles inward using Loop Subdivision to smooth out corners
        triangles.flatMap { it.vertices }.distinct()
            .map { oldVertex ->
                val adjacentVertices = subdividedTriangles
                    .filter { newTriangle -> newTriangle.vertices.contains(oldVertex) }
                    .flatMap { it.edges }
                    .asSequence().distinct()
                    .filter { (a, b) -> oldVertex == a || oldVertex == b }
                    .flatten().distinct()
                    .filter { it != oldVertex }
                    .toList()
                Pair(oldVertex, adjacentVertices)
            }
            .forEach { (oldVertex, adjacentVertices) ->
                val surroundingAverage = adjacentVertices
                    .map { it.location }
                    .fold(Vector3f()) { sum, v -> sum.add(v) }
                    .div(adjacentVertices.size.toFloat())
                // Ratio .46f tested to subdivide icosahedron into spherical shape
                // .625f based on Loop Subdivision method
                oldVertex.location.set(surroundingAverage.lerp(oldVertex.location, .455f))
            }

        val shrinkageRatio = referenceVertex.location.length().div(referenceVertexClone.location.length())
        val resizeFactor = referenceVertexClone.location.length().div(shrinkageRatio)

        subdividedTriangles.flatMap { it.vertices }.distinct()
            .forEach { it.location.mul(resizeFactor) }

        return subdividedTriangles
    }

}

