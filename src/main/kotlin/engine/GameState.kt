package engine

import input.CameraView
import display.Window
import engine.freeBody.Planet
import engine.freeBody.Vehicle
import engine.motion.Motion
import engine.physics.CellLocation
import engine.physics.Gravity
import engine.physics.GravityCell
import game.GamePlayer
import game.GamePlayerTypes
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.World

class GameState {

    val gamePlayers = mutableListOf<GamePlayer>()
    var playerOnTurn: GamePlayer? = null

    lateinit var camera: CameraView

    var world = World(Vec2())
    var vehicles = mutableListOf<Vehicle>()
    var planets = mutableListOf<Planet>()

//    private var worlds = mutableListOf<Planet>()
//    private var asteroids = mutableListOf<Planet>()
//    private var stars = mutableListOf<Planet>()
//    private var warheads = mutableListOf<Planet>()

    val tickables
        get() = vehicles + planets

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

}
