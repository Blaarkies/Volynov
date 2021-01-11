package display.graphic.vertex

import engine.motion.Director
import org.jbox2d.common.Vec2
import org.joml.Vector3f
import utility.*
import utility.Common.Pi2
import utility.Common.circleArea
import utility.Common.degreeToRadian
import kotlin.math.*

object BasicSurfaces {

    fun getHemisphere(radius: Float): List<Triangle> {
        return getDisc(radius)
                .also { triangles -> // normalizes 2d scaling
                    val vertices = triangles.flatMap { it.vertices }
                    val maxDistance = vertices.maxByOrNull { it.length() }!!.length()
                    vertices.distinct().forEach {
                        it.mul(1f / maxDistance)
                        // apply sphere shape
                        it.z = (-it.x.pow(2f) - it.y.pow(2f) + 1f).absoluteValue.pow(.5f)
                    }
                }
    }

    fun getDisc(radius: Float): List<Triangle> {
        val expectedCountTriangles = circleArea(radius) * 4
        val actionMap = HashMap<Number, () -> List<Triangle>>()

        listOf(10, 28, 64, 136, 280, 568).withIndex()
                .forEach { (i, count) ->
                    actionMap[count] = {
                        val baseTri = Triangle()
                        val additionalTriangles = getNLevelsOfTriangles(baseTri.edges, i + 1)
                        listOf(baseTri) + additionalTriangles
                    }
                }

        return actionMap.keys.minByOrNull { it.toFloat().minus(expectedCountTriangles).absoluteValue }
                .let { actionMap[it]!!.invoke() }
    }

    private fun getNLevelsOfTriangles(sides: List<List<Vector3f>>,
                                      depth: Int = 1,
                                      iterationCount: Int = 0): List<Triangle> {
        if (depth == iterationCount) {
            return listOf()
        }
        val (addOns, peakFills) = getNearTriangles(sides)
        val sidesOfNewBase = peakFills.map { it.vertices.take(2) }
        val additionalTriangles = getNLevelsOfTriangles(sidesOfNewBase,
                depth,
                iterationCount + 1)
        return addOns + peakFills + additionalTriangles
    }

    private fun getNearTriangles(sides: List<List<Vector3f>>): Pair<List<Triangle>, List<Triangle>> {
        // Step 4 - Add equilateral triangle on each side of new base polygon
        val addOns = sides.map { (a, b) -> getAddOnEqlTriangle(a, b) }

        // Step 5 - Connect peaks...
        val peakFills = addOns.padEnd().windowed(2) // addOn groups
                .flatMap { (a, b) -> getPeakFillTriangles(a, b) }
        return Pair(addOns, peakFills)
    }

    private fun getPeakFillTriangles(a: Triangle,
                                     b: Triangle): List<Triangle> {
        val baseVertex = a.vertices.last()
        val peakA = a.vertices[1]
        val peakB = b.vertices[1]
        val maxHeight = peakA.length()
        val nowHeight = baseVertex.length()

        val connectVertex = baseVertex.mulClone(maxHeight / nowHeight)

        return listOf(
                Triangle(peakA, connectVertex, baseVertex),
                Triangle(connectVertex, peakB, baseVertex)
        )
    }

    private fun getAddOnEqlTriangle(a: Vector3f, b: Vector3f): Triangle {
        val abOrigin = a.lerpClone(b, .5f)
        val height = a.subClone(b).length() * 3f.pow(.5f) * .5f
        return Triangle(a, abOrigin.addClone(b.normal2d(a).mulClone(height)), b)
    }

    val icosahedron: List<Triangle>
        get() {
            val (pointA, pointB) = BasicShapes.polygon5
            val sideLength = Director.getDistance(pointA, pointB)
            val zHeightSideTriangles = sideLength * sqrt(.75f)
            val fixSlantedSides = cos(10.8f * degreeToRadian) // zDistanceBetweenPentagons != height, triangle is slanted
            val zDistanceBetweenPentagons = zHeightSideTriangles * fixSlantedSides

            val zHeightTopPentagon = zDistanceBetweenPentagons * .5f
            val zHeightBottomPentagon = zDistanceBetweenPentagons * -.5f
            val topPentagonVertices = BasicShapes.polygon5
                    .map { (x, y) -> Vector3f(x, y, zHeightTopPentagon) }
            val bottomPentagonVertices = BasicShapes.polygon5
                    .map { (x, y) -> rotateByAngle(x, y, Pi2 * .1f) }
                    .map { (x, y) -> Vector3f(x, y, zHeightBottomPentagon) }

            val radius = topPentagonVertices[0].length()
            val topVertex = Vector3f(0f, 0f, radius)
            val bottomVertex = Vector3f(0f, 0f, -radius)

            val topTriangles = topPentagonVertices.padEnd().windowed(2)
                    .map { (a, b) -> Triangle(a, b, topVertex) }
            val bottomTriangles = bottomPentagonVertices.padEnd().windowed(2)
                    // Normals are calculated on vertex sequence. Bottom faces need to be reversed
                    .map { (a, b) -> Triangle(b, a, bottomVertex) }

            val middleTriangles = topPentagonVertices.padEnd().windowed(2)
                    .zip(bottomPentagonVertices.padEnd().windowed(2))
                    .flatMap { (top, bottom) ->
                        val topA = top[0]
                        val topB = top[1]
                        val bottomA = bottom[0]
                        val bottomB = bottom[1]

                        listOf(Triangle(topA, bottomA, topB),
                                Triangle(bottomA, bottomB, topB))
                    }

            return listOf(topTriangles, bottomTriangles, middleTriangles).flatten()
                    .also { it.flatMap { it.vertices }.distinct().forEach { it.normalize() } }
        }

    fun subdivide(triangles: List<Triangle>, depth: Int = 1): List<Triangle> {
        var newTriangles = triangles
        repeat(depth) { newTriangles = subdivideShape(newTriangles) }
        return newTriangles
    }

    private fun subdivideShape(triangles: List<Triangle>): List<Triangle> {
        val subdividedTriangles = triangles.flatMap { triangle ->
            val topVertex = triangle.vertices[0]
            val leftVertex = triangle.vertices[1]
            val rightVertex = triangle.vertices[2]

            val innerTriangle = triangle.vertices.padEnd().windowed(2)
                    .map { (a, b) -> a.lerpClone(b, .5f) }
                    .let { Triangle(it) }
            val innerLeftVertex = innerTriangle.vertices[0]
            val innerBottomVertex = innerTriangle.vertices[1]
            val innerRightVertex = innerTriangle.vertices[2]
            val topTriangle = Triangle(innerLeftVertex, innerRightVertex, topVertex)
            val leftTriangle = Triangle(leftVertex, innerBottomVertex, innerLeftVertex)
            val rightTriangle = Triangle(innerBottomVertex, rightVertex, innerRightVertex)

            // Extrude new vertices to have same radius
            innerTriangle.vertices.forEach { it.mul(topVertex.length() / it.length()) }
            listOf(topTriangle, leftTriangle, rightTriangle, innerTriangle)
        }

        // Cleanup extra/duplicate vertices, connect up triangles with common vertices
        val mergeDistance = subdividedTriangles[0].edges[0].let { (a, b) -> a.distance(b) } * .01f
        val allVertices = subdividedTriangles.flatMap { it.vertices }.distinct().withIndex()

        val distincts = allVertices.filter { (index, vertex) ->
            // `vertex` is unique if the first vertex with same position has this `index`
            val firstMatch = allVertices.find { (_, other) -> vertex.distance(other) < mergeDistance }!!
            firstMatch.index == index
        }.map { it.value }
        val duplicates = allVertices.map { it.value }
                .filter { !distincts.contains(it) }

        val duplicatesToDistinctsMap = HashMap<Vector3f, Vector3f>(duplicates.map { duplicate ->
            Pair(// Remove this vertex
                    duplicate,
                    // Use this vertex as replacement
                    distincts.find { distinct -> duplicate.distance(distinct) < mergeDistance }!!)
        }.toMap())

        val mergedTriangles = subdividedTriangles.map { triangle ->
            Triangle(triangle.vertices.map { duplicatesToDistinctsMap[it] ?: it })
        }

        return mergedTriangles
    }

    private fun rotateVecByAngle(vector: Vec2, angle: Float) {
        vector.set(vector.x * cos(angle) - vector.y * sin(angle),
                vector.x * sin(angle) + vector.y * cos(angle))
    }

    private fun rotateByAngle(x: Float, y: Float, angle: Float): List<Float> {
        return listOf(x * cos(angle) - y * sin(angle),
                x * sin(angle) + y * cos(angle))
    }

}

