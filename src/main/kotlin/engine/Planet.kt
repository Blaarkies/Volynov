package engine

import engine.motion.Location
import engine.motion.Motion
import engine.motion.Velocity

class Planet(var name: String) : FreeBody() {

    constructor(
        name: String,
        x: Double, y: Double, h: Double,
        dx: Double, dy: Double, dh: Double,
        mass: Double = 100.0,
        temperature: Double = 325.0,
        radius: Double = 20.0
    ) : this(name) {
        this.mass = mass
        this.temperature = temperature
        this.radius = radius
        motion = Motion(Location(x, y, h), Velocity(dx, dy, dh))
    }

}
