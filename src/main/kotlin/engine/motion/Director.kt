package engine.motion

import kotlin.math.*

object Director {

    fun getDistance(
        serverX: Double, serverY: Double,
        clientX: Double, clientY: Double
    ): Double = hypot(clientX - serverX, clientY - serverY)

    fun getMagnitude(serverX: Double, serverY: Double): Double = getDistance(serverX, serverY, .0, .0)

    /**
     * Computes the angle from [client] to [server] in the range `[0..2Pi]`
     */
    fun getDirection(
        serverX: Double, serverY: Double,
        clientX: Double, clientY: Double
    ): Double {
        val theta = atan2(serverY - clientY, serverX - clientX)
        return when {
            theta < 0 -> theta + 2 * PI
            else -> theta
        }
    }

    /**
     * Computes the angle from [client] to [server] in the range `[0..2Pi]`
     */
    fun getDirection(serverX: Double, serverY: Double): Double = getDirection(serverX, serverY, .0, .0)

}

fun main() {
    (0..20).map {
        val t = 2.0 * PI * (it.toDouble() / 20.toDouble())
        arrayOf((cos(t) * 100).roundToInt(), (sin(t) * 100).roundToInt())
    }
        .map { (x, y) -> Triple(x.toDouble(), y.toDouble(), "($x,$y)") }
        .map { (x, y, string) ->
            Pair(
            Director.getDirection(x, y, 0.0, 0.0)
//                atan2(y, x)
                , string
            )
        }
        .forEach { (direction, place) ->
            println("from $place $direction")
        }

    (0..20).map { (it.toDouble() / 20) * 2 * PI }
        .map { Pair((cos(it) * 100).toInt(), it) }
        .forEach { (xForce, pi) ->
            //            println("$xForce $pi")
        }

}
