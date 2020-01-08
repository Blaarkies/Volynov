package engine.physics

import engine.Planet
import engine.Vehicle
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class GravityTest {

    @Test
    fun gravitationalForce() {
        var planet = Planet("terra", .0, .0, .0, .0, .0, .0)
        var satellite = Vehicle("sputnik", -100.0, -100.0, .0, .0, .0, .0)
        var force = Gravity.gravitationalForce(planet, satellite)
        assertTrue(force.x > 0)
        assertTrue(force.y > 0)

        planet = Planet("terra", .0, .0, .0, .0, .0, .0)
        satellite = Vehicle("sputnik", -100.0, 100.0, .0, .0, .0, .0)
        force = Gravity.gravitationalForce(planet, satellite)
        assertTrue(force.x > 0)
        assertTrue(force.y < 0)

        planet = Planet("terra", .0, .0, .0, .0, .0, .0)
        satellite = Vehicle("sputnik", 100.0, 100.0, .0, .0, .0, .0)
        force = Gravity.gravitationalForce(planet, satellite)
        assertTrue(force.x < 0)
        assertTrue(force.y < 0)

        planet = Planet("terra", .0, .0, .0, .0, .0, .0)
        satellite = Vehicle("sputnik", 100.0, -100.0, .0, .0, .0, .0)
        force = Gravity.gravitationalForce(planet, satellite)
        assertTrue(force.x < 0)
        assertTrue(force.y > 0)
    }
}
