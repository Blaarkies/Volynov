package engine

import display.Window
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
        tickVehicles()

        tickWarheads()
        tickParticles(timeStep)
    }

    private fun tickVehicles() {
        vehicles.forEach { vehicle ->
            if (vehicle.isOutOfGravityField && vehicle.worldBody.linearVelocity.length() > .1f) {
                vehicle.knock(5f * vehicle.worldBody.mass * vehicle.worldBody.linearVelocity.length(),
                    Director.getDirection(vehicle.worldBody.linearVelocity.negate()))
            }
            vehicle.updateLastGravityForce()
        }
    }

    private fun tickParticles(timeStep: Float) {
        particles.toList().forEach { it.update(timeStep, particles) }
    }

    private fun tickWarheads() {
        warheads.toList()
            .filter { it.ageTime > it.selfDestructTime || it.isOutOfGravityField }
            .forEach { detonateWarhead(it) }

        warheads.forEach { it.updateLastGravityForce() }
    }

    private fun detonateWarhead(warhead: Warhead, body: Body? = null) {
        val particle = Particle("1", particles, world, body ?: warhead.worldBody, warhead.worldBody.position, 2f, 1000f)

        checkToDamageVehicles(particle, warhead)
        knockFreeBodies(particle, warhead)

        world.destroyBody(warhead.worldBody)
        warheads.remove(warhead)
    }

    private fun knockFreeBodies(particle: Particle, warhead: Warhead) {
        gravityBodies.map {
            Pair(it, (Director.getDistance(it.worldBody, particle.worldBody)
                    - it.radius - warhead.radius).coerceAtLeast(0f))
        }
            .filter { (_, distance) -> distance < particle.radius }
            .forEach { (body, distance) ->
                val intensity = (1f - distance / particle.radius).coerceAtLeast(0f) * .5f
                val momentum = intensity * warhead.energy

                body.knock(momentum, Director.getDirection(body.worldBody, particle.worldBody))
            }
    }

    private fun checkToDamageVehicles(particle: Particle, warhead: Warhead) {
        vehicles.toList().map {
            Pair(it, (Director.getDistance(it.worldBody, particle.worldBody)
                    - it.radius - warhead.radius).coerceAtLeast(0f))
        }
            .filter { (_, distance) -> distance < particle.radius }
            .forEach { (vehicle, distance) ->
                val damageUnit = (1f - distance / particle.radius).coerceAtLeast(0f)
                    .let { Common.getTimingFunctionEaseOut(it) }
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

    fun fireWarhead(player: GamePlayer,
                    warheadType: String = "will make this some class later",
                    callback: (Warhead) -> Unit) {
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

        val warheadMass = 1f

        activeCallbacks.add {
            vehicle.knock(warheadMass * warheadVelocity.length(), angle + PI.toFloat())

            Particle("1", particles, world, vehicle.worldBody, warheadLocation, .3f, 250f)

            Warhead("1", warheads,
                world, player, warheadLocation.x, warheadLocation.y, angle,
                warheadVelocity.x, warheadVelocity.y, 0f,
                warheadMass, warheadRadius, .1f, .1f,
                onCollision = { self, body -> detonateWarhead(self as Warhead, body) }
            ).also { callback(it) }
        }
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

