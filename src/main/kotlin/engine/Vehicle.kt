package engine

import engine.motion.Location
import engine.motion.Motion
import engine.motion.Velocity

class Vehicle(
    var name: String,
    var hitPoints: Double = 100.0,
    var funds: Int = 1000,// TODO: list of players
    var kills: Int = 0,// TODO: list of players
    var deaths: Int = 0
) : FreeBody() {

    constructor(
        name: String,
        x: Double, y: Double, h: Double,
        dx: Double, dy: Double, dh: Double,
        mass: Double = 1.0,
        temperature: Double = 325.0,
        radius: Double = 10.0
    ) : this(name) {
        this.mass = mass
        this.temperature = temperature
        this.radius = radius
        motion = Motion(Location(x, y, h), Velocity(dx, dy, dh))
    }

}
