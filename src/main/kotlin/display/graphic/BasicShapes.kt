package display.graphic

import engine.motion.Director
import org.jbox2d.common.Vec2
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object BasicShapes {

    val polygon4 = getPolygonVertices(4)

    val polygon5 = getPolygonVertices(5)

    val polygon7 = getPolygonVertices(7)

    val polygon9 = getPolygonVertices(9)

    val polygon15 = getPolygonVertices(15)

    val polygon30 = getPolygonVertices(30)

    private fun getPolygonVertices(corners: Int): List<Float> = (0 until corners).flatMap {
        val t = 2 * PI * (it / corners.toFloat()) + PI * .25
        listOf(cos(t).toFloat(), sin(t).toFloat())
    }

    fun getArrowHeadPoints(linePoints: List<Float>): List<Float> {
        val (ax, ay, bx, by) = linePoints

        val normalY = bx - ax
        val normalX = -by + ay
        val magnitude = Director.getDistance(normalX, normalY)

        val headSize = 5f
        val x = headSize * normalX / magnitude
        val y = headSize * normalY / magnitude
        return listOf(
            bx + x,
            by + y,

            bx - x,
            by - y,

            bx + y,
            by - x
        )
    }

    fun getLineTriangleStrip(
        points: List<Float>,
        startWidth: Float = 4f,
        endWidth: Float = startWidth,
        wrapAround: Boolean = false
    ): List<Float> {
        val lineSegmentsLastIndex = points.lastIndex.toFloat() / 2f

        return points.chunked(2)
            .let { if (wrapAround) it + listOf(it[0]) else it }
            .let { getRightPaddedList(it) }
            .windowed(2)
            .map { (a, b) -> getNormalAndLocationPair(b, a) }
            .let { getLeftPaddedList(it) }
            .windowed(2)
            .map { (a, b) ->
                val averageNormal = a.first.add(b.first)
                averageNormal.normalize()
                Pair(averageNormal, b.second)
            }
            .let {
                when {
                    wrapAround -> {
                        val connect = getNormalPairConnectingPiece(it)
                        listOf(connect) + it.subList(1, it.lastIndex) + connect
                    }
                    else -> it
                }
            }
            .let { getLineEdgePoints(it, lineSegmentsLastIndex, startWidth, endWidth) }
    }

    private fun getNormalPairConnectingPiece(it: List<Pair<Vec2, List<Float>>>): Pair<Vec2, List<Float>> {
        val a = it[it.lastIndex - 1].second
        val b = it[it.lastIndex].second
        val normalAB = Vec2(-b[1] + a[1], b[0] - a[0])
        normalAB.normalize()

        val c = it[0].second
        val d = it[1].second
        val normalCD = Vec2(-d[1] + c[1], d[0] - c[0])
        normalCD.normalize()

        val normal = normalAB.add(normalCD)
        normal.normalize()

        return Pair(normal, it[0].second)
    }

    private fun getLeftPaddedList(it: List<Pair<Vec2, List<Float>>>) =
        listOf(it.first()) + it

    private fun getNormalAndLocationPair(
        b: List<Float>,
        a: List<Float>
    ): Pair<Vec2, List<Float>> {
        val normal = Vec2(-b[1] + a[1], b[0] - a[0])
        normal.normalize()
        return Pair(normal, a)
    }

    private fun getRightPaddedList(locations: List<List<Float>>): List<List<Float>> {
        val (ax, ay) = locations[locations.lastIndex]
        val (bx, by) = locations[locations.lastIndex - 1]
        return locations + listOf(listOf(2f * ax - bx, 2f * ay - by))
    }

    private fun getLineEdgePoints(
        normalsAndLocations: List<Pair<Vec2, List<Float>>>,
        lineSegmentsLastIndex: Float,
        startWidth: Float,
        endWidth: Float
    ): List<Float> {
        return normalsAndLocations.withIndex()
            .flatMap { (index, normalAndLocation) ->
                val (normal, location) = normalAndLocation
                val (ax, ay) = location

                val interpolationDistance = index.toFloat() / lineSegmentsLastIndex
                val width = startWidth * interpolationDistance + endWidth * (1f - interpolationDistance)
                val x = width * normal.x
                val y = width * normal.y
                listOf(
                    ax + x,
                    ay + y,

                    ax - x,
                    ay - y
                )
            }
    }
}
