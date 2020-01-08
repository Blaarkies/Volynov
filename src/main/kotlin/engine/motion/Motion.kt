package engine.motion

import java.util.*


class Motion(
    var location: Location = Location(),
    var velocity: Velocity = Velocity(),
    var acceleration: Acceleration = Acceleration(),
    var contactEvents: MutableList<ContactEvent> = mutableListOf(),
    var trailers: Queue<Trailer> = LinkedList(),
    var trailerQuantity: Int = 20
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
        acceleration.remove()
    }

    private fun addNewTrailer() {
        val distance = location.getDistance(lastTrailer.location)
        if (distance > 20) { // TODO: 5 should be configurable
            val nowTrailer = Trailer(location)
            lastTrailer = nowTrailer

            trailers.add(nowTrailer)
            if (trailers.size > trailerQuantity) {
                trailers.poll()
            }
        }
    }
}
