package engine.physics

import engine.FreeBody
import engine.UniversalConstants
import engine.motion.Director.getDirection
import engine.motion.Director.getDistance
import org.jbox2d.common.Vec2
import utilities.Utils
import kotlin.math.cos
import kotlin.math.sin

object Gravity {

    private const val G = UniversalConstants.gravitationalConstant

    fun gravitationalForce(server: FreeBody, client: FreeBody): Vec2 {
        val m = client.worldBody!!.mass
        val M = server.worldBody!!.mass
        val r = getDistance(server.worldBody!!, client.worldBody!!)
        val forceOnClient = G * m * M / (r * r)

        val direction = getDirection(server.worldBody!!, client.worldBody!!)

        val xF = forceOnClient * cos(direction)
        val yF = forceOnClient * sin(direction)

        return Vec2(xF.toFloat(), yF.toFloat())
    }

    fun addGravityForces(freeBodies: List<FreeBody>) {
        Utils.joinListsNoDuplicate(freeBodies, freeBodies)
            .forEach { (server, client) -> client.worldBody!!.applyForceToCenter(gravitationalForce(server, client)) }
    }

}
