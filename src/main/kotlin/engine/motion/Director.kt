package engine.motion

import engine.physics.CellLocation
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import kotlin.math.*

object Director {

    fun getDistance(serverX: Float, serverY: Float, clientX: Float = 0f, clientY: Float = 0f): Float =
        hypot(clientX - serverX, clientY - serverY)

    fun getDistance(server: Body, client: Body): Float =
        hypot(client.position.x - server.position.x, client.position.y - server.position.y)

    fun getDistance(server: CellLocation, client: CellLocation): Float =
        getDistance(server.x.toFloat(), server.y.toFloat(), client.x.toFloat(), client.y.toFloat())

    /**
     * Computes the angle from [client] to [server] in the range from `-PI` to `PI` radians`
     */
    fun getDirection(serverX: Float, serverY: Float, clientX: Float = 0f, clientY: Float = 0f): Float =
        atan2(serverY - clientY, serverX - clientX)

    fun getDirection(server: Body, client: Body): Float =
        atan2(server.position.y - client.position.y, server.position.x - client.position.x)

    fun getDirection(vector: Vec2): Float = atan2(vector.y, vector.x)

}
