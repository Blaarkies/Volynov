package engine

import engine.motion.Acceleration
import engine.motion.Force
import engine.motion.Motion

class AccelerationRecord(var motion: Motion, var acceleration: Acceleration) {

    constructor(freeBody: FreeBody, force: Force)
            : this(freeBody.motion, Acceleration(force.x / freeBody.mass, force.y / freeBody.mass))

}
