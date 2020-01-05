package engine.physics

import engine.Planet
import engine.Vehicle
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class GravityTest {

    @Test
    fun gravitationalForce() {
        val planet = Planet("terra", .0, .0, .0, .0, .0, .0)
        val satellite = Vehicle("sputnik")
        satellite.motion.location.addLocation(-100.0, -100.0)

        val force = Gravity.gravitationalForce(planet, satellite)
        assertTrue(force.x > 0)
        assertTrue(force.y > 0)
    }
}
