package engine.freeBody

import dI
import display.draw.Model
import display.draw.TextureConfig
import display.draw.TextureEnum
import display.event.MouseButtonEvent
import display.graphic.vertex.BasicShapes
import display.graphic.Color
import display.graphic.vertex.BasicSurfaces
import engine.gameState.GameState
import engine.gameState.GameState.Companion.getContactBodies
import engine.physics.CollisionBits
import game.shield.VehicleShield
import game.GamePlayer
import game.PlayerAim
import game.fuel.Fuel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*
import utility.Common
import utility.Common.Pi
import utility.Common.makeVec2
import utility.Common.makeVec2Circle
import kotlin.math.pow

class Vehicle(
    vehicles: MutableList<Vehicle>,
    world: World,
    val player: GamePlayer,
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
        val fullShape = BasicShapes.polygon4Spiked
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
                    Vec2())
                    .toTypedArray()
                shapeBox.set(vertices, vertices.size)
                FixtureDef().also {
                    it.shape = shapeBox
                    it.density = mass / (Pi * radius.pow(2f) * (fullShape.size * .5f))
                    it.friction = friction
                    it.restitution = restitution
                    it.filter.categoryBits = CollisionBits.vehicle
                    it.filter.maskBits = CollisionBits.planetVehicleWarhead or CollisionBits.border
                }
            }
            .forEach { worldBody.createFixture(it) }

        textureConfig = TextureConfig(texture, Common.vectorUnit.mul(.7f),
            chunkedVertices = listOf(listOf(0f, 0f)) + fullShape + listOf(fullShape.first()),
            color = color)
            .updateGpuBufferData()

        player.vehicle = this
        vehicles.add(this)

        model = Model(
            triangles = BasicSurfaces.getHemisphere(radius),
            texture = texture
        )
    }

    private val unsubscribe = PublishSubject.create<Boolean>()
    var shield: VehicleShield? = null
    var hitPoints = 100f

    val isStable: Boolean
        get() = worldBody.contactList != null && getContactBodies(worldBody.contactList)
            .filter { it.userData !is MapBorder }
            .any { other -> other.mass > 10f }
    var hasCollided = false

    private var lastUpdatedAt = dI.gameState.tickTime

    var fuel: Fuel? = null

    fun update(tickTime: Float) {
        fuel?.burn(tickTime)
        shield?.update(tickTime)
    }

    fun fireWarhead(
        gameState: GameState,
        player: GamePlayer,
        callback: (Warhead) -> Unit
    ) {
        val angle = player.playerAim.angle
        val power = player.playerAim.power * .15f
        val originLocation = worldBody.position
        val originVelocity = worldBody.linearVelocity

        val warheadRadius = .2f
        val minimumSafeDistance = 1.5f * radius
        val angleVector = makeVec2Circle(angle)

        val warheadLocation = angleVector.mul(minimumSafeDistance).add(originLocation)
        val warheadVelocity = angleVector.mul(power).add(originVelocity)

        val warheadMass = 1f

        gameState.activeCallbacks.add {
            shield?.setShieldOnTurn()

            knock(warheadMass * warheadVelocity.length(), angle + Pi)

            Particle("1", gameState.particles, gameState.world, worldBody.linearVelocity, warheadLocation,
                radius = .3f, duration = 250f, createdAt = gameState.tickTime)

            Warhead("1", gameState.warheads,
                gameState.world, player, warheadLocation.x, warheadLocation.y, angle + 1.5f * Pi,
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

    fun startJump(playerAim: PlayerAim) {
        fuel = Fuel.create(playerAim.selectedFuel, lastUpdatedAt, this)
        fuel?.startJump(playerAim)
    }

    fun addShield(playerAim: PlayerAim) {
        if (shield != null) {
            shield?.dispose()
        }
        shield = VehicleShield.create(playerAim.selectedShield, this)
    }

    fun thrustVehicle(event: Observable<MouseButtonEvent>) {
        fuel?.startThrust()
        event.takeUntil(unsubscribe).doOnComplete { fuel?.endThrust() }.subscribe()
    }

    fun dispose(world: World, vehicles: MutableList<Vehicle>) {
        unsubscribe.onNext(true)

        world.destroyBody(worldBody)
        vehicles.remove(this)
    }

    fun inflictDamage(amount: Float, firedBy: GamePlayer) {
        hitPoints -= shield?.blockDamage(amount, firedBy) ?: amount
    }

}
