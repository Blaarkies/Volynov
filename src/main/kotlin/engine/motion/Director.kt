package engine.motion

import org.jbox2d.dynamics.Body
import kotlin.math.*

object Director {

    fun getDistance(serverX: Float, serverY: Float, clientX: Float = 0f, clientY: Float = 0f): Float =
        hypot(clientX - serverX, clientY - serverY)

    fun getDistance(server: Body, client: Body): Float =
        hypot(client.position.x - server.position.x, client.position.y - server.position.y)

    /**
     * Computes the angle from [client] to [server] in the range from `-PI` to `PI` radians`
     */
    fun getDirection(serverX: Float, serverY: Float, clientX: Float = 0f, clientY: Float = 0f): Float =
        atan2(serverY - clientY, serverX - clientX)

    fun getDirection(server: Body, client: Body): Float =
        atan2(server.position.y - client.position.y, server.position.x - client.position.x)

}
