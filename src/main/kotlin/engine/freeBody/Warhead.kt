package engine.freeBody

import display.draw.TextureConfig
import display.draw.TextureEnum
import display.graphic.BasicShapes
import display.graphic.Color
import engine.FreeBodyCallback
import engine.motion.Director
import game.GamePlayer
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.World
import utility.Common

class Warhead(
    id: String,
    warheads: MutableList<Warhead>,
    world: World,
    val firedBy: GamePlayer,
    x: Float,
    y: Float,
    h: Float,
    dx: Float,
    dy: Float,
    dh: Float,
    mass: Float,
    radius: Float = .7F,
    restitution: Float = .3f,
    friction: Float = .6f,
    onCollision: (FreeBody, Body?) -> Unit
) : FreeBody(id, radius) {

    val freeBodyCallback = FreeBodyCallback(this, onCollision)

    init {
        val shapeBox = PolygonShape()
        val vertices = BasicShapes.polygon4.chunked(2)
            .map { Vec2(it[0] * radius * 2f, it[1] * radius) }
            .toTypedArray()
        shapeBox.set(vertices, vertices.size)

        val bodyDef = createBodyDef(BodyType.DYNAMIC, x, y, h, dx, dy, dh)
        worldBody = createWorldBody(shapeBox, mass, radius, friction, restitution, world, bodyDef)
        worldBody.isBullet = true
        worldBody.userData = this

        textureConfig = TextureConfig(TextureEnum.metal,
            chunkedVertices = shapeBox.vertices.map { listOf(it.x / radius, it.y / radius) },
            color = Color.createFromHsv(0f, 1f, .3f, 1f))
            .updateGpuBufferData()

        firedBy.warheads.add(this)
        warheads.add(this)
    }

    private val currentTime
        get() = System.currentTimeMillis()

    val ageTime
        get() = (currentTime - createdAt)

    private val createdAt = currentTime
    val selfDestructTime = 45000f
    // TODO: player in current aiming phase could just wait out this time if they wanted to
    // also influences score

    val damage = 50f
    val energy = 50f

    var lastGravityForce: Float = 0f
    val isOutOfGravityField: Boolean
        get() {
            val nowGravityForce = worldBody.m_force.length()
            return false //nowGravityForce < 0.02f
        }

    fun updateLastGravityForce() {
        lastGravityForce = worldBody.m_force.length()
    }

    fun detonate(world: World,
                 warheads: MutableList<Warhead>,
                 particles: MutableList<Particle>,
                 vehicles: MutableList<Vehicle>,
                 gravityBodies: List<FreeBody>,
                 impacted: Body? = null) {
        val particle = Particle("1", particles, world, impacted ?: worldBody, worldBody.position, 2f, 1000f)

        checkToDamageVehicles(world, vehicles, particle)
        knockFreeBodies(gravityBodies, particle)

        world.destroyBody(worldBody)
        warheads.remove(this)
    }

    private fun knockFreeBodies(gravityBodies: List<FreeBody>, particle: Particle) {
        gravityBodies.map {
            Pair(it, (Director.getDistance(it.worldBody, particle.worldBody)
                    - it.radius - radius).coerceAtLeast(0f))
        }
            .filter { (_, distance) -> distance < particle.radius }
            .forEach { (body, distance) ->
                val intensity = (1f - distance / particle.radius).coerceAtLeast(0f) * .5f
                val momentum = intensity * energy

                body.knock(momentum, Director.getDirection(body.worldBody, particle.worldBody))
            }
    }

    private fun checkToDamageVehicles(world: World,
                                      vehicles: MutableList<Vehicle>,
                                      particle: Particle) {
        vehicles.toList().map {
            Pair(it, (Director.getDistance(it.worldBody, particle.worldBody)
                    - it.radius - radius).coerceAtLeast(0f))
        }
            .filter { (_, distance) -> distance < particle.radius }
            .forEach { (vehicle, distance) ->
                val damageUnit = (1f - distance / particle.radius).coerceAtLeast(0f)
                    .let { Common.getTimingFunctionEaseOut(it) }
                val totalDamage = damageUnit * damage
                vehicle.hitPoints -= totalDamage
                firedBy.scoreDamage(this, totalDamage, vehicle)

                if (vehicle.hitPoints <= 0) {
                    firedBy.scoreKill(vehicle)

                    world.destroyBody(vehicle.worldBody)
                    vehicles.remove(vehicle)
                }
            }
    }

    fun update(world: World,
               warheads: MutableList<Warhead>,
               particles: MutableList<Particle>,
               vehicles: MutableList<Vehicle>,
               gravityBodies: List<FreeBody>) {
        if (ageTime > selfDestructTime || isOutOfGravityField) {
            detonate(world, warheads, particles, vehicles, gravityBodies)
        }
        updateLastGravityForce()
    }

}
