package display.graphic.vertex

import org.joml.Vector3f
import utility.*
import utility.Common.circleArea
import kotlin.math.absoluteValue
import kotlin.math.pow

object BasicSurfaces {

    fun getHemisphere(radius: Float): List<Triangle> {
        return getDisc(radius)
            .also { triangles -> // normalizes 2d scaling
                val vertices = triangles.flatMap { it.vertices }
                val maxDistance = vertices.maxBy { it.length() }!!.length()
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
                    val additionalTriangles = getNLevelsOfTriangles(baseTri.sides, i + 1)
                    listOf(baseTri) + additionalTriangles
                }
            }

        return actionMap.keys.minBy { it.toFloat().minus(expectedCountTriangles).absoluteValue }
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

}

