package engine.physics

import engine.freeBody.FreeBody
import engine.motion.Director.getDirection
import engine.motion.Director.getDistance
import org.jbox2d.common.Vec2
import utilities.Utils
import kotlin.math.cos
import kotlin.math.sin

object Gravity {

    private const val G = UniversalConstants.gravitationalConstant

    fun gravitationalForce(server: FreeBody, client: FreeBody): Vec2 {
        val m = client.worldBody.mass
        val M = server.worldBody.mass
        val r = getDistance(server.worldBody, client.worldBody)
        val forceOnClient = G * m * M / (r * r)

        val direction = getDirection(server.worldBody, client.worldBody)

        val xF = forceOnClient * cos(direction)
        val yF = forceOnClient * sin(direction)

        return Vec2(xF, yF)
    }

    fun addGravityForces(freeBodies: List<FreeBody>) {
//        val clientLists = mutableListOf<List<Pair<FreeBody, Vec2>>>()
//
//        Utils.joinListsNoDuplicate(freeBodies, freeBodies)
//            .groupBy { (_, client) -> client }.toList()
//            .chunked(40)
//            .map { list ->
//                val clients = mutableListOf<Pair<FreeBody, Vec2>>()
//                clientLists.add(clients)
//                thread(start = true) {
//                    list.map { (client, servers) ->
//                        val totalForce = servers
//                            .map { (server, client) -> gravitationalForce(server, client) }
//                            .reduce { acc, force -> acc.add(force) }
//                        clients.add(Pair(client, totalForce))
//                    }
//                }
//            }
//            .forEach { it.join() }
//
//        clientLists.flatten()
//            .forEach { (client, totalForce) -> client.worldBody.applyForceToCenter(totalForce) }


        Utils.joinListsNoDuplicate(freeBodies, freeBodies)
            .groupBy { (_, client) -> client }
            .forEach { (client, servers) ->
                val totalForce = servers
                    .map { (server, client) -> gravitationalForce(server, client) }
                    .reduce { acc, force -> acc.add(force) }
                client.worldBody.applyForceToCenter(totalForce)
            }
    }

}

