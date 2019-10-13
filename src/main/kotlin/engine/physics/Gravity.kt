package engine.physics

import engine.FreeBody
import engine.UniversalConstants
import engine.motion.Force
import kotlin.math.cos
import kotlin.math.sin


object Gravity {

    fun gravitationalForce(server: FreeBody, client: FreeBody): Force {
        val G = UniversalConstants.gravitationalConstant
        val m = client.mass
        val M = server.mass
        val r = client.getDistance(server)
        val forceOnClient = G * m * M / (r * r)

        val direction = client.getDirection(server)

        val xF = forceOnClient * cos(direction)
        val yF = forceOnClient * sin(direction)

        return Force(xF, yF)
    }

}
