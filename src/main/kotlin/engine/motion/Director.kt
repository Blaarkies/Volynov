package engine.motion

import org.jbox2d.dynamics.Body
import kotlin.math.*

object Director {

    fun getDistance(
        serverX: Double, serverY: Double,
        clientX: Double, clientY: Double
    ): Double = hypot(clientX - serverX, clientY - serverY)

    fun getDistance(serverX: Double, serverY: Double): Double = getDistance(serverX, serverY, .0, .0)

    fun getDistance(serverX: Float, serverY: Float): Float = hypot(.0 - serverX, .0 - serverY).toFloat()

    fun getDistance(server: Body, client: Body): Double =
        hypot(client.position.x - server.position.x, client.position.y - server.position.y).toDouble()

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

    fun getDirection(server: Body, client: Body): Double =
        atan2(server.position.y - client.position.y, server.position.x - client.position.x).toDouble()

}
