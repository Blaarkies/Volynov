package display.graphic.vertex

import engine.motion.Director
import org.jbox2d.common.Vec2
import utility.Common
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object BasicShapes {

    val polygon3 = getPolygonVertices(3)

    val polygon4 = getPolygonVertices(4)

    val polygon5 = getPolygonVertices(5)

    val polygon6 = getPolygonVertices(6)

    val polygon7 = getPolygonVertices(7)

    val polygon8 = getPolygonVertices(8)

    val polygon9 = getPolygonVertices(9)

    val polygon15 = getPolygonVertices(15)

    val polygon30 = getPolygonVertices(30)

    val square = polygon4.map { it * sqrt(2f) }

    val polygon4Spiked = getSpikedPolygon(8)

    val verticalLine = listOf(0f, 1f, 0f, -1f)

    val squareHouse = polygon4.chunked(2)
        .let { it.subList(0, 1) + listOf(listOf(0f, 1.05f)) + it.subList(1, 4) }
        .flatten()

    private fun getPolygonVertices(corners: Int, rotate: Float = 1f.div(corners)): List<Float> =
        (0 until corners).flatMap {
            val t = Common.Pi2 * (it.toFloat() / corners.toFloat()) + Common.Pi * rotate
            listOf(cos(t), sin(t))
        }

    private fun getSpikedPolygon(corners: Int, smoothness: Float = .6f): List<Float> {
        return getPolygonVertices(corners).chunked(2)
            .withIndex()
            .flatMap { (index, vertex) ->
                val scale = (if (index.rem(2) == 0) 1f else smoothness)
                listOf(vertex[0] * scale, vertex[1] * scale)
            }
    }

    fun getArrowHeadPoints(linePoints: List<Float>, headSize: Float = 1f): List<Float> {
        val (ax, ay, bx, by) = linePoints

        val normalY = bx - ax
        val normalX = -by + ay
        val magnitude = Director.getDistance(normalX, normalY)

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

    private fun getLeftPaddedList(it: List<Pair<Vec2, List<Float>>>) = listOf(it.first()) + it

    private fun getNormalAndLocationPair(b: List<Float>, a: List<Float>): Pair<Vec2, List<Float>> {
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
                val width = endWidth * interpolationDistance + startWidth * (1f - interpolationDistance)
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
