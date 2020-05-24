package engine

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

    var mapBorder: MapBorder? = null
    val gamePlayers = mutableListOf<GamePlayer>()
    var playerOnTurn: GamePlayer? = null

    lateinit var camera: CameraView

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
        vehicles.forEach { it.update() }
    }

    private fun tickParticles() {
        particles.toList().forEach { it.update(tickTime, particles) }
    }

    private fun tickWarheads() {
        warheads.toList().forEach { it.update(world, tickTime, warheads, particles, vehicles, gravityBodies) }
    }

    fun reset() {
        gamePlayers.clear()
        camera.reset()
        world = World(Vec2())
        tickTime = 0f
        vehicles.clear()
        planets.clear()

        world.setContactListener(GameContactListener(this))
    }

    fun clone(): GameState {
        return GameState().also {
            it.world = World(Vec2())
            it.world.setContactListener(GameContactListener(it))
            it.gamePlayers.addAll(gamePlayers.map { oldPlayer -> cloneGamePlayer(oldPlayer) })
            it.playerOnTurn = it.gamePlayers.find { newPlayer -> newPlayer.name == playerOnTurn?.name }
            it.camera = camera
            vehicles.forEach { oldVehicle ->
                cloneVehicle(oldVehicle, it.vehicles, it.world,
                    gamePlayers.find { oldPlayer -> oldPlayer.vehicle == oldVehicle }!!.name
                        .let { oldName ->
                            it.gamePlayers.find { newPlayer -> newPlayer.name == oldName }!!
                        }
                )
            }
            planets.forEach { planet -> clonePlanet(planet, it.planets, it.world) }
            warheads.forEach { warhead ->
                cloneWarhead(warhead, it.warheads, it.world,
                    gamePlayers.find { oldPlayer -> oldPlayer.warheads.contains(warhead) }!!.name
                        .let { oldName ->
                            it.gamePlayers.find { newPlayer -> newPlayer.name == oldName }!!
                        },
                    it
                )
            }
            //            it.particles = mutableListOf<Particle>()
            it.mapBorder = cloneMapBorder(mapBorder!!,
                planets.find { planet -> planet.id == mapBorder!!.mapCenterBody.id }!!,
                it.world)
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

        fun cloneGamePlayer(it: GamePlayer): GamePlayer =
            GamePlayer(it.name, it.type, null, PlayerAim(it.playerAim.angle, it.playerAim.power), 0f, 0f)

        fun cloneVehicle(it: Vehicle,
                         vehicles: MutableList<Vehicle>,
                         world: World,
                         player: GamePlayer): Vehicle {
            val body = it.worldBody
            return Vehicle(vehicles, world, player,
                body.position.x, body.position.y, body.angle,
                body.linearVelocity.x, body.linearVelocity.y, body.angularVelocity,
                body.mass, it.radius,
                body.fixtureList.restitution, body.fixtureList.friction,
                it.textureConfig.texture, it.textureConfig.color)
        }

        fun clonePlanet(it: Planet,
                        planets: MutableList<Planet>,
                        world: World): Planet {
            val body = it.worldBody
            return Planet(it.id, planets, world,
                body.position.x, body.position.y, body.angle,
                body.linearVelocity.x, body.linearVelocity.y, body.angularVelocity,
                body.mass, it.radius,
                body.fixtureList.restitution, body.fixtureList.friction,
                it.textureConfig.texture)
        }

        fun cloneWarhead(it: Warhead,
                         warheads: MutableList<Warhead>,
                         world: World,
                         firedBy: GamePlayer,
                         gameState: GameState): Warhead {
            val body = it.worldBody
            return Warhead(it.id, warheads, world, firedBy,
                body.position.x, body.position.y, body.angle,
                body.linearVelocity.x, body.linearVelocity.y, body.angularVelocity,
                body.mass, it.radius,
                body.fixtureList.restitution, body.fixtureList.friction,
                onCollision = { self, impacted ->
                    (self as Warhead).detonate(gameState.world, gameState.tickTime, gameState.warheads,
                        gameState.particles, gameState.vehicles, gameState.gravityBodies, impacted)
                },
                createdAt = it.createdAt)
        }

        fun cloneMapBorder(it: MapBorder, mapCenterBody: FreeBody, world: World): MapBorder =
            MapBorder(mapCenterBody, world, it.radius)

    }
}

