package engine.motion

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AccelerationTest {

    private var acceleration = Acceleration()

    @Test
    fun getRelativeAcceleration() {
        acceleration = Acceleration()
        var clientAcceleration = Acceleration(1.0, 1.0)
        var relativeAcceleration = acceleration.getRelativeAcceleration(clientAcceleration)
        assertEquals(1.0, relativeAcceleration.ddx)
        assertEquals(1.0, relativeAcceleration.ddy)

        clientAcceleration = Acceleration(-1.0, -1.0)
        relativeAcceleration = acceleration.getRelativeAcceleration(clientAcceleration)
        assertEquals(-1.0, relativeAcceleration.ddx)
        assertEquals(-1.0, relativeAcceleration.ddy)

        clientAcceleration = Acceleration(1.0, 1.0)
        val invertedRelativeAcceleration = clientAcceleration.getRelativeAcceleration(acceleration)
        assertEquals(-1.0, invertedRelativeAcceleration.ddx)
        assertEquals(-1.0, invertedRelativeAcceleration.ddy)
    }

    @Test
    fun addAcceleration() {
        acceleration = Acceleration()
        acceleration.addAcceleration(1.0, 1.0, 1.0)
        assertEquals(1.0, acceleration.ddx)
        assertEquals(1.0, acceleration.ddy)
        assertEquals(1.0, acceleration.ddh)

        acceleration = Acceleration()
        acceleration.addAcceleration(Acceleration(1.0, 1.0, 1.0))
        assertEquals(1.0, acceleration.ddx)
        assertEquals(1.0, acceleration.ddy)
        assertEquals(1.0, acceleration.ddh)

        acceleration = Acceleration()
        acceleration.addAcceleration(Force(2.0, 2.0), 2.0)
        assertEquals(1.0, acceleration.ddx)
        assertEquals(1.0, acceleration.ddy)
        assertEquals(0.0, acceleration.ddh)
    }

}