package engine

import display.Window
import engine.freeBody.Particle
import engine.freeBody.Planet
import engine.freeBody.Vehicle
import engine.freeBody.Warhead
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
        vehicles.forEach { it.update() }
    }

    private fun tickParticles(timeStep: Float) {
        particles.toList().forEach { it.update(timeStep, particles) }
    }

    private fun tickWarheads() {
        warheads.toList().forEach { it.update(world, warheads, particles, vehicles, gravityBodies) }
    }

    fun reset() {
        gamePlayers.clear()
        camera.reset()
        world = World(Vec2())
        vehicles.clear()
        planets.clear()

        world.setContactListener(ContactListener(this))
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

