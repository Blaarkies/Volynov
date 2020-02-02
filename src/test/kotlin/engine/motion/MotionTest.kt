package engine.motion

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.util.*

internal class MotionTest {

    @Test
    fun update_location_changes() {
        val subject = Motion(velocity = Velocity(10.0, 20.0, 30.0))
        assertEquals(.0, subject.location.x)
        assertEquals(.0, subject.location.y)
        assertEquals(.0, subject.location.h)

        subject.updateLocationChanges()
        assertEquals(10.0, subject.location.x)
        assertEquals(20.0, subject.location.y)
        assertEquals(30.0, subject.location.h)
    }

    @Test
    fun create_new_trailers() {
        val subject = Motion(velocity = Velocity(10.0, 20.0))
        assertEquals(1, subject.trailers.size)
        var lastTrailer = (subject.trailers as LinkedList).last
        assertEquals(.0, lastTrailer.location.x)
        assertEquals(.0, lastTrailer.location.y)

        subject.updateLocationChanges()
        assertEquals(2, subject.trailers.size)
        lastTrailer = (subject.trailers as LinkedList).last
        assertEquals(10.0, lastTrailer.location.x)
        assertEquals(20.0, lastTrailer.location.y)
    }

    @Test
    fun update_velocity_changes() {
        val subject = Motion(acceleration = Acceleration(10.0, 20.0, 30.0))
        assertEquals(.0, subject.velocity.dx)
        assertEquals(.0, subject.velocity.dy)
        assertEquals(.0, subject.velocity.dh)

        subject.updateVelocityChanges()
        assertEquals(10.0, subject.velocity.dx)
        assertEquals(20.0, subject.velocity.dy)
        assertEquals(30.0, subject.velocity.dh)
    }
}
