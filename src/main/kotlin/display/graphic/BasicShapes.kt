package display.graphic

import engine.motion.Director
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object BasicShapes {

    val polygon9 = (0 until 9)
        .flatMap {
            val t = 2.0 * PI * (it.toFloat() / 9)
            listOf(cos(t).toFloat(), sin(t).toFloat())
        }

    val polygon15 = (0 until 15)
        .flatMap {
            val t = 2.0 * PI * (it.toFloat() / 15)
            listOf(cos(t).toFloat(), sin(t).toFloat())
        }

    val polygon30 = (0 until 30)
        .flatMap {
            val t = 2.0 * PI * (it.toFloat() / 30)
            listOf(cos(t).toFloat(), sin(t).toFloat())
        }

    fun getArrowHeadPoints(linePoints: List<Float>): List<Float> {
        val ax = linePoints[0]
        val ay = linePoints[1]
        val bx = linePoints[2]
        val by = linePoints[3]

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
                val ax = chunk[0]
                val ay = chunk[1]
                val bx = chunk[2]
                val by = chunk[3]

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
