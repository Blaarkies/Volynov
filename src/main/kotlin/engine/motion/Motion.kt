package engine.motion

import java.util.*


class Motion(
    var location: Location = Location(),
    var velocity: Velocity = Velocity(),
    var acceleration: Acceleration = Acceleration(),
    var contactEvents: MutableList<ContactEvent> = mutableListOf(),
    var trailers: Queue<Trailer> = LinkedList(),
    var trailerQuantity: Int = 50
) {

    private var lastTrailer: Trailer = Trailer(location)

    init {
        trailers.add(lastTrailer)
    }

    fun updateLocationChanges() {
        location.addLocation(velocity.dx, velocity.dy, velocity.dy)
        addNewTrailer()
    }

    fun updateVelocityChanges() {
        velocity.addVelocity(acceleration.ddx, acceleration.ddy, acceleration.ddh)
    }

    private fun addNewTrailer() {
        val distance = location.getDistance(lastTrailer.location)
        if (distance > 5) { // TODO: 5 should be configurable
            val nowTrailer = Trailer(location)
            lastTrailer = nowTrailer

            trailers.add(nowTrailer)
            if (trailers.size > trailerQuantity) {
                trailers.poll()
            }
        }
    }
}
