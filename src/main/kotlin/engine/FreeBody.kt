package engine

import engine.motion.Acceleration
import engine.motion.Location
import engine.motion.Motion
import engine.motion.Velocity

open class FreeBody(
    var motion: Motion,
    var mass: Double = 1.0,
    var temperature: Double = .0,
    var radius: Double = .0
) {

    val angularMass: Double
        get() = calculateAngularMass()

    constructor(
        mass: Double = 1.0,
        temperature: Double = .0,
        radius: Double = .0,
        x: Double = .0, y: Double = .0, h: Double = .0,
        dx: Double = .0, dy: Double = .0, dh: Double = .0,
        trailersPopulation: Int = 25
    ) : this(
        Motion(Location(x, y, h), Velocity(dx, dy, dh), trailerQuantity = trailersPopulation),
        mass, temperature, radius
    )

    private fun calculateAngularMass(): Double {
        val area = radius * 4
        return mass * area // TODO: find real equation
    }

    fun getRelativeAcceleration(client: FreeBody): Acceleration {
        return motion.acceleration.getRelativeAcceleration(client.motion.acceleration)
    }

    fun getRelativeVelocity(client: FreeBody): Velocity {
        return motion.velocity.getRelativeVelocity(client.motion.velocity)
    }

    fun getDistance(client: FreeBody): Double {
        return motion.location.getDistance(client.motion.location)
    }

    fun getDirection(client: FreeBody): Double {
        return motion.location.getDirection(client.motion.location)
    }

}
