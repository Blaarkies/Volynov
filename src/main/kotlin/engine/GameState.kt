package engine

import display.CameraView
import engine.freeBody.Planet
import engine.freeBody.Vehicle
import engine.motion.Motion
import engine.physics.CellLocation
import engine.physics.Gravity
import engine.physics.GravityCell
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.World

class GameState {

    var camera = CameraView()
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

    private fun tickGravityChanges() {
        Gravity.addGravityForces(tickables)
            .let { (gravityMap, resolution) ->
                this.gravityMap = gravityMap
                this.resolution = resolution
            }
    }

    fun tickClock(
        world: World,
        timeStep: Float,
        velocityIterations: Int,
        positionIterations: Int
    ) {
        world.step(timeStep, velocityIterations, positionIterations)

        tickGravityChanges()
        Motion.addNewTrailers(tickables.filter { it.radius > .5f })
    }
}
