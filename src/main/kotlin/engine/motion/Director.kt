package engine.motion

import engine.physics.CellLocation
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import kotlin.math.atan2
import kotlin.math.hypot

object Director {

    fun getDistance(serverX: Float, serverY: Float, clientX: Float = 0f, clientY: Float = 0f): Float =
        hypot(clientX - serverX, clientY - serverY)

    fun getDistance(server: Body, client: Body): Float = getDistance(server.position, client.position)

    fun getDistance(server: Vec2, client: Vec2): Float = hypot(client.x - server.x, client.y - server.y)

    fun getDistance(server: CellLocation, client: CellLocation): Float =
        getDistance(server.x.toFloat(), server.y.toFloat(), client.x.toFloat(), client.y.toFloat())

    /**
     * Computes the angle from [client] to [server] in the range from `-PI` to `PI` radians`
     */
    fun getDirection(serverX: Float, serverY: Float, clientX: Float = 0f, clientY: Float = 0f): Float =
        atan2(serverY - clientY, serverX - clientX)

    fun getDirection(server: Body, client: Body): Float = getDirection(server.position, client.position)

    fun getDirection(server: Vec2, client: Vec2): Float = atan2(server.y - client.y, server.x - client.x)

    fun getDirection(vector: Vec2): Float = atan2(vector.y, vector.x)

}
