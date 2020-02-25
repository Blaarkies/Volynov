package engine

import display.CameraView
import engine.motion.Motion
import engine.physics.Contact
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

    private fun tickLocationChanges() {
        Motion.addLocationChanges(locationTickables)
    }

    private fun tickVelocityChanges() {
        Motion.addVelocityChanges(locationTickables)
    }

    private fun tickGravityChanges() {
        Gravity.addGravityForces(locationTickables)
    }

    private fun tickCollisionChanges() {
        Contact.addCollisionForces(locationTickables)
    }

    private fun tickFrictionChanges() {
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

//        tickLocationChanges()
//        tickGravityChanges()
//        tickCollisionChanges()
//            tickFrictionChanges()
//        tickVelocityChanges()
    }
}
