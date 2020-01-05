package engine.motion

import kotlin.math.*

object Director {

    fun getDistance(
        serverX: Double, serverY: Double,
        clientX: Double, clientY: Double
    ): Double = hypot(clientX - serverX, clientY - serverY)

    fun getMagnitude(serverX: Double, serverY: Double): Double = getDistance(serverX, serverY, .0, .0)

    /**
     * Computes the angle from [client] to [server] in the range from `-PI` to `PI` radians`
     */
    fun getDirection(
        serverX: Double, serverY: Double,
        clientX: Double, clientY: Double
    ): Double = atan2(serverY - clientY, serverX - clientX)

    /**
     * Computes the angle from [0, 0] to [server] in the range from `-PI` to `PI` radians`
     */
    fun getDirection(serverX: Double, serverY: Double): Double = getDirection(serverX, serverY, .0, .0)

}
