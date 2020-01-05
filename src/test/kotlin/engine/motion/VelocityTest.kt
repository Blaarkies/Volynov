package engine.motion

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class VelocityTest {

    @Test
    fun getRelativeVelocity() {
        var server = Velocity(1.0, 2.0)
        var client = Velocity(1.0, 2.0)
        var relative = server.getRelativeVelocity(client)

        assertEquals(.0, relative.dx)
        assertEquals(.0, relative.dy)

        server = Velocity(-1.0, -2.0)
        client = Velocity(1.0, 2.0)
        relative = server.getRelativeVelocity(client)

        assertEquals(2.0, relative.dx)
        assertEquals(4.0, relative.dy)
    }

    @Test
    fun addVelocity() {
        val subject = Velocity()
        assertEquals(.0, subject.dx)
        assertEquals(.0, subject.dy)
        assertEquals(.0, subject.dh)

        subject.addVelocity(1.0, 2.0, 3.0)
        assertEquals(1.0, subject.dx)
        assertEquals(2.0, subject.dy)
        assertEquals(3.0, subject.dh)
    }
}
