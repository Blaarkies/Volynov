package engine.motion

import engine.FreeBody
import java.util.*


class Motion(
    var location: Location = Location(),
    var velocity: Velocity = Velocity(),
    var acceleration: Acceleration = Acceleration(),
    var trailers: Queue<Trailer> = LinkedList(),
    var trailerQuantity: Int = 80,

    var debugLastAcceleration: Acceleration = Acceleration(acceleration)
) {

    private var lastTrailer: Trailer = Trailer(location)

    init {
        trailers.add(lastTrailer)
    }

    fun updateLocationChanges() {
        location.add(velocity.dx, velocity.dy, velocity.dh)
        addNewTrailer()
    }

    fun updateVelocityChanges() {
        velocity.add(acceleration.ddx, acceleration.ddy, acceleration.ddh)
        debugLastAcceleration = Acceleration(acceleration)
        acceleration.remove()
    }

    private fun addNewTrailer() {
        val distance = location.getDistance(lastTrailer.location)
        if (distance > 5) {
            val nowTrailer = Trailer(location)
            lastTrailer = nowTrailer

            trailers.add(nowTrailer)
            if (trailers.size > trailerQuantity) {
                trailers.poll()
            }
        }
    }

    companion object {

        fun addLocationChanges(freeBodies: List<FreeBody>) {
            freeBodies.forEach { it.motion.updateLocationChanges() }
        }

        fun addVelocityChanges(freeBodies: List<FreeBody>) {
            freeBodies.forEach { it.motion.updateVelocityChanges() }
        }

        fun addNewTrailers(freeBodies: List<FreeBody>) {
            freeBodies.forEach {
                it.motion.location = it.worldBody!!.position.let { Location(it.x.toDouble(), it.y.toDouble()) }
                it.motion.addNewTrailer()
            }
        }

    }
}
