package display.graphic

import engine.motion.Director
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
        val t = 2 * PI * (it / corners.toFloat())
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

    fun getTriangleStripPoints(
        points: List<Float>,
        startWidth: Float = 4f,
        endWidth: Float = startWidth
    ): List<Float> {
        val lineSegmentsLastIndex = points.lastIndex.toFloat() / 4f

        return points
            .chunked(4)
            .withIndex()
            .flatMap { (index, chunk) ->
                val (ax, ay, bx, by) = chunk

                val normalY = bx - ax
                val normalX = -by + ay
                val magnitude = Director.getDistance(normalX, normalY)

                val interpolationDistance = index.toFloat() / lineSegmentsLastIndex
                val width = startWidth * interpolationDistance + endWidth * (1f - interpolationDistance)
                val x = width * normalX / magnitude
                val y = width * normalY / magnitude
                listOf(
                    ax + x,
                    ay + y,

                    ax - x,
                    ay - y,

                    bx + x,
                    by + y,

                    bx - x,
                    by - y
                )
            }
    }
}
