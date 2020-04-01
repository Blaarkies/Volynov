package engine

import input.CameraView
import display.Window
import display.draw.TextureConfig
import display.draw.TextureHolder
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
import org.jbox2d.common.MathUtils.cos
import org.jbox2d.common.MathUtils.sin
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.World

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

        tickGravityChanges()
        Motion.addNewTrailers(trailerBodies)

        particles.toList().forEach {
            it.worldBody.position.addLocal(it.worldBody.linearVelocity.mul(timeStep))
            if (it.ageTime > 1000f) {
                particles.remove(it)
            }
        }

        warheads.toList().forEach { warhead ->
            val contactList = warhead.worldBody.contactList
            if (contactList != null && contactList.contact.isTouching) {
                val particle = warhead.createParticles(particles, world, contactList.other)

                world.destroyBody(warhead.worldBody)
                warheads.remove(warhead)

                vehicles.map { Pair(it, Director.getDistance(it.worldBody, particle.worldBody)) }
                    .filter { (_, distance) -> distance < particle.radius }
                    .forEach { (vehicle, distance) ->
                        val damage = (100f / particle.radius) * (particle.radius - distance / particle.radius)
                        vehicle.hitPoints -= damage
                    }
            }
        }
    }

    fun reset() {
        gamePlayers.clear()
        camera.reset()
        world = World(Vec2())
        vehicles.clear()
        planets.clear()
    }

    fun fireWarhead(
        textures: TextureHolder,
        player: GamePlayer,
        warheadType: String = "will make this some class later"
    ): Warhead {
        checkNotNull(player.vehicle) { "Player does not have a vehicle." }
        val vehicle = player.vehicle!!
        val angle = player.playerAim.angle
        val power = player.playerAim.power * .15f
        val origin = vehicle.worldBody.position
        val originVelocity = vehicle.worldBody.linearVelocity

        val warheadRadius = .2f
        val minimumSafeDistance = 2f * vehicle.radius
        val warheadLocation = Vec2(
            origin.x + cos(angle) * minimumSafeDistance,
            origin.y + sin(angle) * minimumSafeDistance
        )
        val warheadVelocity = Vec2(originVelocity.x + cos(angle) * power, originVelocity.y + sin(angle) * power)

        return Warhead.create(
            world, player, warheadLocation.x, warheadLocation.y, angle,
            warheadVelocity.x, warheadVelocity.y, 0f,
            .1f, warheadRadius, textureConfig = TextureConfig(textures.metal)
        )
            .let {
                warheads.add(it)
                it
            }

    }

}
