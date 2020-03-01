package engine

import display.CameraView
import engine.freeBody.Planet
import engine.freeBody.Vehicle
import engine.motion.Motion
import engine.physics.Gravity
import org.jbox2d.dynamics.World

class GameState {

    var camera = CameraView()
    var vehicles = mutableListOf<Vehicle>()
    var planets = mutableListOf<Planet>()

//    private var worlds = mutableListOf<Planet>()
//    private var asteroids = mutableListOf<Planet>()
//    private var stars = mutableListOf<Planet>()
//    private var warheads = mutableListOf<Planet>()

    private val locationTickables
        get() = vehicles + planets

    private fun tickGravityChanges() {
        Gravity.addGravityForces(locationTickables)
    }

    fun tickClock(
        world: World,
        timeStep: Float,
        velocityIterations: Int,
        positionIterations: Int
    ) {

        tickGravityChanges()

        world.step(timeStep, velocityIterations, positionIterations)

        Motion.addNewTrailers(locationTickables.filter { it.radius > 10 })
    }
}
