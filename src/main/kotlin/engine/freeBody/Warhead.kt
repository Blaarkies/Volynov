package engine.freeBody

import dI
import display.draw.ModelEnum
import display.draw.TextureConfig
import display.draw.TextureEnum
import display.graphic.Color
import display.graphic.vertex.BasicShapes
import engine.gameState.GameState
import engine.motion.Director
import engine.physics.CollisionBits
import game.GamePlayer
import game.GamePlayerType
import io.reactivex.Observable.just
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.World
import utility.Common
import utility.Common.Pi
import utility.Common.getRandom
import utility.Common.getRandomMixed
import utility.Common.getRandomSign
import utility.Common.makeVec2Circle
import utility.PidController
import utility.WavefrontObject
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue
import kotlin.math.sqrt

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
    radius: Float = .6F,
    restitution: Float = .3f,
    friction: Float = .6f,
    onCollision: (self: FreeBody, impacted: Body?) -> Unit,
    val createdAt: Float
) : FreeBody(id, radius) {

    val rotation = Rotation(0f, .07f * .1f.coerceAtLeast(.2f) * getRandomMixed())
    val freeBodyCallback = FreeBodyCallback(this, onCollision)

    init {
        val shapeBox = PolygonShape()
        val vertices = BasicShapes.squareHouse.chunked(2)
            .map { Vec2(it[0] * radius, it[1] * radius * 1.5f) }
            .toTypedArray()
        shapeBox.set(vertices, vertices.size)

        val bodyDef = createBodyDef(BodyType.DYNAMIC, x, y, h, dx, dy, dh)
        worldBody = createWorldBody(
            shapeBox, mass, radius, friction, restitution,
            world, bodyDef,
            CollisionBits.warhead,
            CollisionBits.planetVehicleWarhead
                    or CollisionBits.shield
                    or CollisionBits.border
        )
        worldBody.isBullet = true
        worldBody.userData = this

        textureConfig = TextureConfig(
            TextureEnum.metal,
            chunkedVertices = shapeBox.vertices.map { listOf(it.x / radius, it.y / radius) },
            color = Color.createFromHsv(0f, 1f, .3f, 1f)
        )
            .updateGpuBufferData()

        firedBy.warheads.add(this)
        warheads.add(this)

        model = dI.models.getModel(ModelEnum.missile_basic)
    }

    var hitPoints = 100f
    val selfDestructTime = 45000f
    private var lastUpdatedAt = createdAt
    private val updateInterval = 333f

    private val damage = 50f
    private val energy = 50f
    private var fuel = 5f
    private val angleController = PidController(-.3f, .01f, -.2f)

    fun detonate(
        world: World,
        tickTime: Float,
        warheads: MutableList<Warhead>,
        particles: MutableList<Particle>,
        vehicles: MutableList<Vehicle>,
        gravityBodies: List<FreeBody>,
        impacted: Body? = null
    ) {
        val particle = Particle(
            "1", particles, world, impacted?.linearVelocity ?: worldBody.linearVelocity,
            worldBody.position, radius = 2f, duration = 1000f, createdAt = tickTime
        )

        checkToDamageVehicles(tickTime, vehicles, particle)
        knockFreeBodies(gravityBodies, particle)
        particle.radius = 0f

        dispose(world, warheads)

        if (firedBy.type != GamePlayerType.CLONE) {
            just(0)
                .delay(1000, TimeUnit.MILLISECONDS)
                .takeUntil(dI.cameraView.unsubscribeCheckCameraEvent)
                .subscribe { dI.cameraView.checkCameraEvent() }
        }
    }

    fun dispose(world: World, warheads: MutableList<Warhead>) {
        world.destroyBody(worldBody)
        warheads.remove(this)
    }

    private fun knockFreeBodies(gravityBodies: List<FreeBody>, particle: Particle) {
        gravityBodies.map {
            Pair(
                it,
                (Director.getDistance(it.worldBody, particle.worldBody)
                        - it.radius - radius).coerceAtLeast(0f)
            )
        }
            .filter { (_, distance) -> distance < particle.radius }
            .forEach { (body, distance) ->
                val intensity = (1f - distance / particle.radius).coerceAtLeast(0f) * .5f
                val momentum = intensity * energy

                body.knock(momentum, Director.getDirection(body.worldBody, particle.worldBody))
            }
    }

    private fun checkToDamageVehicles(
        tickTime: Float,
        vehicles: MutableList<Vehicle>,
        particle: Particle
    ) {
        vehicles.toList().map {
            Pair(
                it, (Director.getDistance(it.worldBody, particle.worldBody)
                        - it.radius - radius).coerceAtLeast(0f)
            )
        }
            .filter { (_, distance) -> distance < particle.radius }
            .forEach { (vehicle, distance) ->
                val damageUnit = (1f - distance / particle.radius).coerceAtLeast(0f)
                    .let { Common.getTimingFunctionEaseOut(it) }
                val totalDamage = damageUnit * damage
                vehicle.inflictDamage(totalDamage, firedBy)
                firedBy.scoreDamage(this, totalDamage, vehicle, tickTime)

                if (vehicle.hitPoints <= 0) {
                    firedBy.scoreKill(vehicle)
                    //                    vehicle.dispose(world, vehicles)
                }
            }
    }

    fun update(
        world: World,
        tickTime: Float,
        warheads: MutableList<Warhead>,
        particles: MutableList<Particle>,
        vehicles: MutableList<Vehicle>,
        gravityBodies: List<FreeBody>
    ) {
        rotation.update()
        if (getRestTime(tickTime) > updateInterval) {
            lastUpdatedAt = tickTime

            if (fuel > 0f) {
                rotateTowardsVelocity(world, tickTime, particles)
            }

            if (getAgeTime(tickTime) > selfDestructTime) {
                detonate(world, tickTime, warheads, particles, vehicles, gravityBodies)
            }
        }
    }

    private fun getRestTime(tickTime: Float) = tickTime - lastUpdatedAt

    fun getAgeTime(tickTime: Float) = tickTime - createdAt

    private fun rotateTowardsVelocity(
        world: World,
        tickTime: Float,
        particles: MutableList<Particle>
    ) {
        val a = worldBody.linearVelocity.clone().also { it.normalize() }
        val b = makeVec2Circle(worldBody.angle)

        val dotProduct = a.x * b.x + a.y * b.y

        val reaction = angleController.getReaction(dotProduct, 0f)

        twist(reaction)
        val reactionSize = reaction.absoluteValue
        fuel = (fuel - reactionSize).coerceAtLeast(0f)

        if (reactionSize > .03f) {
            val scaledReaction = reactionSize * 3f
            val side = if (reaction < 0f) 0f else Pi
            val exhaustDirection = makeVec2Circle(worldBody.angle - side)

            val tailLocation = makeVec2Circle(worldBody.angle - Pi * .5f).mul(radius)
            val location = worldBody.position.add(tailLocation)

            particles.add(
                Particle(
                    "puff", particles, world, worldBody.linearVelocity, location,
                    exhaustDirection.mul(3f), sqrt(scaledReaction) * .7f,
                    scaledReaction * 100f + 300f, TextureEnum.rcs_puff, Color.WHITE.setAlpha(.3f), tickTime
                )
            )
        }
    }

    fun clone(
        warheads: MutableList<Warhead>,
        world: World,
        firedBy: GamePlayer,
        gameState: GameState
    ): Warhead {
        val body = worldBody
        return Warhead(
            id, warheads, world, firedBy,
            body.position.x, body.position.y, body.angle,
            body.linearVelocity.x, body.linearVelocity.y, body.angularVelocity,
            body.mass, radius,
            body.fixtureList.restitution, body.fixtureList.friction,
            onCollision = { self, impacted ->
                (self as Warhead).detonate(
                    gameState.world, gameState.tickTime,
                    gameState.warheads,
                    gameState.particles, gameState.vehicles, gameState.gravityBodies, impacted
                )
            },
            createdAt = createdAt
        )
    }

    fun sustainDamage(damage: Float) {
        hitPoints -= damage

        if (hitPoints <= 0f) {
            val world = dI.gameState.world
            val tickTime = dI.gameState.tickTime
            val warheads = dI.gameState.warheads
            val particles = dI.gameState.particles
            val vehicles = dI.gameState.vehicles
            val gravityBodies = dI.gameState.gravityBodies
            dI.gameState.activeCallbacks.add {
                detonate(world, tickTime, warheads, particles, vehicles, gravityBodies)
            }
        }
    }

}
