package display.graphic.vertex

import org.joml.Vector3f
import utility.*
import kotlin.math.pow

object BasicSurfaces {

    fun getHemisphere(radius: Float): List<Vector3f> {
        return getDisc().flatMap { it.vertices }
    }

    fun getDisc(): List<Triangle> {
        val baseTri = Triangle()

        val additionalTriangles = getNLevelsOfTriangles(
            baseTri.sides,
            5)
        return listOf(baseTri) + additionalTriangles
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

