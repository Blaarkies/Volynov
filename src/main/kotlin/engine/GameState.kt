package engine

import input.CameraView
import display.Window
import display.draw.TextureConfig
import display.draw.TextureHolder
import display.graphic.BasicShapes
import engine.freeBody.Planet
import engine.freeBody.Vehicle
import engine.freeBody.Warhead
import engine.motion.Motion
import engine.physics.CellLocation
import engine.physics.Gravity
import engine.physics.GravityCell
import game.GamePlayer
import game.GamePlayerTypes
import org.jbox2d.common.MathUtils.cos
import org.jbox2d.common.MathUtils.sin
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.World

class GameState {

    val gamePlayers = mutableListOf<GamePlayer>()
    var playerOnTurn: GamePlayer? = null

    lateinit var camera: CameraView

    var world = World(Vec2())
    var vehicles = mutableListOf<Vehicle>()
    var planets = mutableListOf<Planet>()
    var warheads = mutableListOf<Warhead>()

//    private var worlds = mutableListOf<Planet>()
//    private var asteroids = mutableListOf<Planet>()
//    private var stars = mutableListOf<Planet>()

    val tickables
        get() = vehicles + planets + warheads

    var gravityMap = HashMap<CellLocation, GravityCell>()
    var resolution = 0f

    fun init(window: Window) {
        camera = CameraView(window)
    }

    private fun tickGravityChanges() {
        Gravity.addGravityForces(tickables)
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
        Motion.addNewTrailers(tickables.filter { it.radius > .5f })
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
        val minimumSafeDistance = 3f * vehicle.radius
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
