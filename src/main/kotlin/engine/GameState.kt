package engine

import display.Window
import display.draw.TextureConfig
import display.draw.TextureEnum
import display.graphic.Color
import engine.freeBody.Particle
import engine.freeBody.Planet
import engine.freeBody.Vehicle
import engine.freeBody.Warhead
import engine.motion.Director
import engine.motion.Motion
import engine.physics.CellLocation
import engine.physics.Gravity
import engine.physics.GravityCell
import game.GamePlayer
import input.CameraView
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.World
import org.jbox2d.dynamics.contacts.Contact
import org.jbox2d.dynamics.contacts.ContactEdge
import utility.Common
import utility.Common.makeVec2Circle
import kotlin.math.PI

class GameState {

    val gamePlayers = mutableListOf<GamePlayer>()
    var playerOnTurn: GamePlayer? = null

    lateinit var camera: CameraView

    var world = World(Vec2())
    val vehicles = mutableListOf<Vehicle>()
    val planets = mutableListOf<Planet>()
    val warheads = mutableListOf<Warhead>()
    val particles = mutableListOf<Particle>()

    //    private var asteroids = mutableListOf<Planet>()
    //    private var stars = mutableListOf<Planet>()

    val gravityBodies
        get() = vehicles + planets + warheads

    val trailerBodies
        get() = vehicles + warheads

    var gravityMap = HashMap<CellLocation, GravityCell>()
    var resolution = 0f
    val activeCallbacks = mutableListOf<() -> Unit>()

    fun init(window: Window) {
        camera = CameraView(window)
    }

    private fun tickGravityChanges() {
        Gravity.addGravityForces(gravityBodies)
            .let { (gravityMap, resolution) ->
                this.gravityMap = gravityMap
                this.resolution = resolution
            }
    }

    fun tickClock(
        timeStep: Float,
        velocityIterations: Int,
        positionIterations: Int
    ) {
        world.step(timeStep, velocityIterations, positionIterations)

        activeCallbacks.forEach { it() }
        activeCallbacks.clear()

        tickGravityChanges()
        Motion.addNewTrailers(trailerBodies)

        tickWarheads()
        tickParticles(timeStep)
    }

    private fun tickParticles(timeStep: Float) {
        particles.toList().forEach {
            it.worldBody.position.addLocal(it.worldBody.linearVelocity.mul(timeStep))
            if (it.ageTime > 1000f) {
                particles.remove(it)
                return@forEach
            }

            val scale = Common.getTimingFunctionEaseOut(it.ageTime / 1000f)
            it.radius = it.fullRadius * scale
        }
    }

    private fun tickWarheads() {
        warheads.toList()
            .filter { it.ageTime > it.selfDestructTime || it.isOutOfGravityField }
            .forEach { detonateWarhead(it) }

        warheads.toList().forEach { it.checkGravityField() }
    }

    private fun detonateWarhead(warhead: Warhead, body: Body? = null) {
        val particle = warhead.createParticles(particles, world, body ?: warhead.worldBody)

        checkToDamageVehicles(particle, warhead)

        world.destroyBody(warhead.worldBody)
        warheads.remove(warhead)
    }

    private fun checkToDamageVehicles(particle: Particle, warhead: Warhead) {
        vehicles.toList().map {
            Pair(it, (Director.getDistance(it.worldBody, particle.worldBody)
                    - it.radius - warhead.radius).coerceAtLeast(0f))
        }
            .filter { (_, distance) -> distance < particle.radius }
            .forEach { (vehicle, distance) ->
                val damageUnit = (1f - distance / particle.radius).coerceAtLeast(0f)
                    .let { Common.getTimingFunctionEaseIn(it) }
                val totalDamage = damageUnit * warhead.damage
                vehicle.hitPoints -= totalDamage
                warhead.firedBy.scoreDamage(warhead, totalDamage, vehicle)

                if (vehicle.hitPoints <= 0) {
                    warhead.firedBy.scoreKill(vehicle)

                    world.destroyBody(vehicle.worldBody)
                    vehicles.remove(vehicle)
                }
            }
    }

    fun reset() {
        gamePlayers.clear()
        camera.reset()
        world = World(Vec2())
        vehicles.clear()
        planets.clear()

        world.setContactListener(ContactListener(this))
    }

    fun fireWarhead(player: GamePlayer, warheadType: String = "will make this some class later"): Warhead {
        checkNotNull(player.vehicle) { "Player does not have a vehicle." }
        val vehicle = player.vehicle!!
        val angle = player.playerAim.angle
        val power = player.playerAim.power * .15f
        val originLocation = vehicle.worldBody.position
        val originVelocity = vehicle.worldBody.linearVelocity

        val warheadRadius = .2f
        val minimumSafeDistance = 1.5f * vehicle.radius
        val angleVector = makeVec2Circle(angle)

        val warheadLocation = angleVector.mul(minimumSafeDistance).add(originLocation)
        val warheadVelocity = angleVector.mul(power).add(originVelocity)

        val warheadMass = .1f

        activeCallbacks.add { vehicle.knock(warheadMass * warheadVelocity.length(), angle + PI.toFloat()) }

        return Warhead.create(
            world, player, warheadLocation.x, warheadLocation.y, angle,
            warheadVelocity.x, warheadVelocity.y, 0f,
            warheadMass, warheadRadius,
            textureConfig = TextureConfig(TextureEnum.metal, color = Color.createFromHsv(0f, 1f, .3f, 1f)),
            onWarheadCollision = { self, body -> detonateWarhead(self as Warhead, body) }
        )
            .also { warheads.add(it) }

    }

    companion object {

        fun getContactBodies(contactEdge: ContactEdge): Sequence<Body> = sequence {
            var currentContact = contactEdge
            yield(currentContact.other)

            while (currentContact.next != null) {
                yield(currentContact.other)
                currentContact = currentContact.next
            }
        }

        fun getContactEdges(contactEdge: ContactEdge): Sequence<Contact> = sequence {
            var currentContact = contactEdge
            yield(currentContact.contact)

            while (currentContact.next != null) {
                yield(currentContact.contact)
                currentContact = currentContact.next
            }
        }

    }
}

