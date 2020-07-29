package engine.gameState

import dI
import display.Window
import engine.freeBody.*
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

class GameState {

    var background: MapBackground = MapBackground()

    var mapBorder: MapBorder? = null
    val gamePlayers = mutableListOf<GamePlayer>()
    var playerOnTurn: GamePlayer? = null

    var world = World(Vec2())
    var tickTime = 0f
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

    private fun tickGravityChanges() {
        Gravity.addGravityForces(gravityBodies)
            .let { (gravityMap, resolution) ->
                this.gravityMap = gravityMap
                this.resolution = resolution
            }
    }

    fun tickClock(timeStep: Float, velocityIterations: Int, positionIterations: Int) {
        world.step(timeStep, velocityIterations, positionIterations)
        tickTime += timeStep * 1000f

        activeCallbacks.forEach { it() }
        activeCallbacks.clear()

        tickGravityChanges()
        Motion.addNewTrailers(trailerBodies)
        tickVehicles()

        tickWarheads()
        tickParticles()

        mapBorder?.update()
    }

    private fun tickVehicles() {
        vehicles.forEach { it.update(tickTime) }
    }

    private fun tickParticles() {
        particles.toList().forEach { it.update(tickTime, particles) }
    }

    private fun tickWarheads() {
        warheads.toList().forEach { it.update(world, tickTime, warheads, particles, vehicles, gravityBodies) }
    }

    fun reset() {
        gamePlayers.clear()
        dI.cameraView.reset()
        world = World(Vec2())
        tickTime = 0f
        vehicles.clear()
        planets.clear()

        world.setContactListener(GameContactListener(this))
    }

    fun clone(): GameState = GameState().also {
        it.world = World(Vec2())
        it.world.setContactListener(GameContactListener(it))
        it.gamePlayers.addAll(gamePlayers.map { oldPlayer -> oldPlayer.clone() })
        it.playerOnTurn = it.gamePlayers.find { newPlayer -> newPlayer.name == playerOnTurn?.name }
        vehicles.forEach { oldVehicle ->
            val playerOwningVehicle = gamePlayers.find { oldPlayer -> oldPlayer.vehicle == oldVehicle }!!.name
                .let { oldName -> it.gamePlayers.find { newPlayer -> newPlayer.name == oldName }!! }
            oldVehicle.clone(it.vehicles, it.world, playerOwningVehicle)
        }
        planets.forEach { planet -> planet.clone(it.planets, it.world) }
        warheads.forEach { warhead ->
            val playerOwningWarhead = gamePlayers.find { oldPlayer -> oldPlayer.warheads.contains(warhead) }!!.name
                .let { oldName -> it.gamePlayers.find { newPlayer -> newPlayer.name == oldName }!! }
            warhead.clone(it.warheads, it.world, playerOwningWarhead, it)
        }
        //            it.particles = mutableListOf<Particle>()
        it.mapBorder = mapBorder?.clone(planets.find { planet -> planet.id == mapBorder!!.mapCenterBody.id }!!,
            it.world)
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
