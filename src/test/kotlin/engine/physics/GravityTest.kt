package engine.physics

import engine.GameState
import engine.Planet
import engine.Vehicle
import engine.motion.Force
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class GravityTest {

    @Test
    fun direction_of_gravitational_force() {
        val forceUpRight = getGravityForceBetweenPlanetSatellite(-100.0, -100.0)
        assertTrue(forceUpRight.x > 0)
        assertTrue(forceUpRight.y > 0)

        val forceDownRight = getGravityForceBetweenPlanetSatellite(-100.0, 100.0)
        assertTrue(forceDownRight.x > 0)
        assertTrue(forceDownRight.y < 0)

        val forceDownLeft = getGravityForceBetweenPlanetSatellite(100.0, 100.0)
        assertTrue(forceDownLeft.x < 0)
        assertTrue(forceDownLeft.y < 0)

        val forceUpLeft = getGravityForceBetweenPlanetSatellite(100.0, -100.0)
        assertTrue(forceUpLeft.x < 0)
        assertTrue(forceUpLeft.y > 0)
    }

    @Test
    fun in_binary_system_the_massive_body_moves_less() {
        val terra = Planet("terra", .0, .0, .0, .0, .0, .0, 9000.0)
        val luna = Planet("luna", 100.0, .0, .0, .0, .0, .0, 500.0)

        val gameState = GameState()
        gameState.planets.add(terra)
        gameState.planets.add(luna)

        assertEquals(.0, terra.motion.acceleration.magnitude)
        assertEquals(.0, luna.motion.acceleration.magnitude)

        gameState.tickClock()

        assertNotEquals(.0, terra.motion.debugLastAcceleration.magnitude)
        assertNotEquals(.0, luna.motion.debugLastAcceleration.magnitude)

        assertTrue(terra.motion.debugLastAcceleration.magnitude < luna.motion.debugLastAcceleration.magnitude)
    }

    private fun getGravityForceBetweenPlanetSatellite(sx: Double = .0, sy: Double = .0): Force {
        val planet = Planet("terra", .0, .0, .0, .0, .0, .0)
        val satellite = Vehicle("sputnik", sx, sy, .0, .0, .0, .0)
        return Gravity.gravitationalForce(planet, satellite)
    }
}
