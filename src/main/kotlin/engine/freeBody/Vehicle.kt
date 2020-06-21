package engine.freeBody

import display.draw.TextureConfig
import display.draw.TextureEnum
import display.graphic.BasicShapes
import display.graphic.Color
import engine.gameState.GameState
import engine.shields.VehicleShield
import game.GamePlayer
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.World
import utility.Common
import utility.Common.makeVec2
import kotlin.math.PI
import kotlin.math.pow

class Vehicle(
    vehicles: MutableList<Vehicle>,
    world: World,
    player: GamePlayer,
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
    texture: TextureEnum,
    color: Color = Color.WHITE
) : FreeBody(player.name, radius) {

    init {
        val fullShape = BasicShapes.polygon4Spiked.chunked(2)
        val bodyDef = createBodyDef(BodyType.DYNAMIC, x, y, h, dx, dy, dh)
        worldBody = world.createBody(bodyDef)
        worldBody.userData = this

        (listOf(fullShape.last()) + fullShape + listOf(fullShape.first()))
            .map { listOf(it[0] * radius, it[1] * radius) }
            .windowed(3, 2)
            .map { (a, b, c) ->
                val shapeBox = PolygonShape()
                val vertices = listOf(
                    makeVec2(a),
                    makeVec2(b),
                    makeVec2(c),
                    Vec2()
                )
                    .toTypedArray()
                shapeBox.set(vertices, vertices.size)
                FixtureDef().also {
                    it.shape = shapeBox
                    it.density = mass / (PI.toFloat() * radius.pow(2f) * (fullShape.size * .5f))
                    it.friction = friction
                    it.restitution = restitution
                }
            }
            .forEach { worldBody.createFixture(it) }

        textureConfig = TextureConfig(texture, Common.vectorUnit.mul(.7f),
            chunkedVertices = listOf(listOf(0f, 0f)) + fullShape + listOf(fullShape.first()),
            color = color)
            .updateGpuBufferData()

        player.vehicle = this
        vehicles.add(this)
    }

    var shield: VehicleShield? = null
    var hitPoints: Float = 100f

    var lastGravityForce: Float = 0f
    val isOutOfGravityField: Boolean
        get() {
            val nowGravityForce = worldBody.m_force.length()
            return false //nowGravityForce < 0.1f
        }
    val isStable: Boolean
        get() = worldBody.contactList != null && GameState.getContactBodies(worldBody.contactList)
            .filter { it.userData !is MapBorder }
            .any { other -> other.mass > 10f }

    fun updateLastGravityForce() {
        lastGravityForce = worldBody.m_force.length()
    }

    fun update() {
        if (isOutOfGravityField && worldBody.linearVelocity.clone().let {
                it.normalize()
                it
            }.add(worldBody.position.clone().let {
                it.normalize()
                it
            }).length() > .7f) {
            worldBody.linearDamping = .8f
        } else {
            worldBody.linearDamping = 0f
        }

        updateLastGravityForce()
    }

    fun fireWarhead(gameState: GameState,
                    player: GamePlayer,
                    warheadType: String = "will make this some class later",
                    callback: (Warhead) -> Unit) {
        val angle = player.playerAim.angle
        val power = player.playerAim.power * .15f
        val originLocation = worldBody.position
        val originVelocity = worldBody.linearVelocity

        val warheadRadius = .2f
        val minimumSafeDistance = 1.5f * radius
        val angleVector = Common.makeVec2Circle(angle)

        val warheadLocation = angleVector.mul(minimumSafeDistance).add(originLocation)
        val warheadVelocity = angleVector.mul(power).add(originVelocity)

        val warheadMass = 1f

        gameState.activeCallbacks.add {
            knock(warheadMass * warheadVelocity.length(), angle + PI.toFloat())

            Particle("1", gameState.particles, gameState.world, worldBody, warheadLocation, .3f, 250f,
                createdAt = gameState.tickTime)

            Warhead("1", gameState.warheads,
                gameState.world, player, warheadLocation.x, warheadLocation.y, angle + 1.5f * PI.toFloat(),
                warheadVelocity.x, warheadVelocity.y, 0f,
                warheadMass, warheadRadius, .1f, .1f,
                onCollision = { self, impacted ->
                    (self as Warhead).detonate(gameState.world, gameState.tickTime, gameState.warheads,
                        gameState.particles, gameState.vehicles, gameState.gravityBodies, impacted)
                },
                createdAt = gameState.tickTime
            ).also { callback(it) }
        }

    }

    fun clone(vehicles: MutableList<Vehicle>, world: World, player: GamePlayer): Vehicle {
        val body = worldBody
        return Vehicle(vehicles, world, player,
            body.position.x, body.position.y, body.angle,
            body.linearVelocity.x, body.linearVelocity.y, body.angularVelocity,
            body.mass, radius,
            body.fixtureList.restitution, body.fixtureList.friction,
            textureConfig.texture, textureConfig.color)
    }

}
