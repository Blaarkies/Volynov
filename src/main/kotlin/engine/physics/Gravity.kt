package engine.physics

import engine.freeBody.FreeBody
import engine.motion.Director.getDirection
import engine.motion.Director.getDistance
import kotlinx.coroutines.*
import org.jbox2d.common.Vec2
import java.util.concurrent.ForkJoinPool
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

object Gravity {

    private const val G = UniversalConstants.gravitationalConstant

    private var coreCount = ForkJoinPool.commonPool().parallelism

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

    fun gravitationalForce(m: Float, x1: Float, y1: Float, M: Float, x2: Float, y2: Float): Vec2 {
        val r = getDistance(x1, y1, x2, y2)
        val forceOnClient = G * m * M / (r * r)

        val direction = getDirection(x1, y1, x2, y2)

        val xF = forceOnClient * cos(direction)
        val yF = forceOnClient * sin(direction)

        return Vec2(xF, yF)
    }

    fun addGravityForces(freeBodies: List<FreeBody>): Pair<HashMap<CellLocation, GravityCell>, Float> = runBlocking {
        val gravityMap = HashMap<CellLocation, GravityCell>()

        val resolution = 20f//2f
        freeBodies.forEach {
            val x = it.worldBody.position.x
            val y = it.worldBody.position.y
            val cellLocation = CellLocation((x / resolution).roundToInt(), (y / resolution).roundToInt())
            gravityMap[cellLocation].let { cell ->
                if (cell == null) {
                    gravityMap[cellLocation] = GravityCell(it.worldBody.mass, mutableListOf(it))
                } else {
                    cell.totalMass += it.worldBody.mass
                    cell.freeBodies.add(it)
                }
            }
        }

        val gravityMapList = gravityMap.toList()
        gravityMapList
            .let { it.chunked((it.size / coreCount).coerceAtLeast(1)) }
            .forEach { list ->

                launch(Dispatchers.Default) {

                    list.forEach { (mainLocation, mainCell) ->
                        mainCell.freeBodies.forEach { clientBody ->
                            val body = clientBody.worldBody
                            val totalForce = Vec2()

                            gravityMapList.filter { (subLocation, _) ->
                                getDistance(
                                    subLocation.x.toFloat(),
                                    subLocation.y.toFloat(),
                                    mainLocation.x.toFloat(),
                                    mainLocation.y.toFloat()
                                ) > 1f
                            }.forEach { (subLocation, serverCell) ->
                                val force = gravitationalForce(
                                    serverCell.totalMass, subLocation.x * resolution, subLocation.y * resolution,
                                    body.mass, body.position.x, body.position.y
                                )
                                totalForce.x += force.x
                                totalForce.y += force.y
                            }

                            gravityMapList.filter { (subLocation, _) ->
                                getDistance(
                                    subLocation.x.toFloat(), subLocation.y.toFloat(),
                                    mainLocation.x.toFloat(), mainLocation.y.toFloat()
                                ) <= 1f
                            }.flatMap { (_, cell) -> cell.freeBodies }
                                .filter { it != clientBody }
                                .forEach { server ->
                                    val serverBody = server.worldBody
                                    val force = gravitationalForce(
                                        serverBody.mass, serverBody.position.x, serverBody.position.y,
                                        body.mass, body.position.x, body.position.y
                                    )
                                    totalForce.x += force.x
                                    totalForce.y += force.y
                                }

                            body.applyForceToCenter(totalForce)
                        }
                    }
                }
            }

        Pair(gravityMap, resolution)
    }

}

