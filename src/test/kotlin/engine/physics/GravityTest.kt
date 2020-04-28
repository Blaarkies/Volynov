package engine.physics

import TestConstants.positionIterations
import TestConstants.timeStep
import TestConstants.velocityIterations
import display.draw.TextureEnum
import engine.freeBody.Planet
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.World
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class GravityTest {

    @Test
    fun direction_of_gravitational_force() {
        val forceUpRight = getGravityForceBetweenPlanetSatellite(-100f, -100f)
        assertTrue(forceUpRight.x > 0)
        assertTrue(forceUpRight.y > 0)

        val forceDownRight = getGravityForceBetweenPlanetSatellite(-100f, 100f)
        assertTrue(forceDownRight.x > 0)
        assertTrue(forceDownRight.y < 0)

        val forceDownLeft = getGravityForceBetweenPlanetSatellite(100f, 100f)
        assertTrue(forceDownLeft.x < 0)
        assertTrue(forceDownLeft.y < 0)

        val forceUpLeft = getGravityForceBetweenPlanetSatellite(100f, -100f)
        assertTrue(forceUpLeft.x < 0)
        assertTrue(forceUpLeft.y > 0)
    }

    @Test
    fun in_binary_system_the_massive_body_moves_less() {
        val world = World(Vec2())

        val planets = mutableListOf<Planet>()
        val terra = Planet("terra", planets, world, -5f, 0f, 0f, 0f, 0f, 0f, 1000f, 1f,
            texture = TextureEnum.white_pixel)
        val luna = Planet("luna", planets, world, 5f, 0f, 0f, 0f, 0f, 0f, 100f, 1f,
            texture = TextureEnum.white_pixel)

        planets.forEach {
            assertTrue(it.worldBody.linearVelocity.length() == 0f, "Expected ${it.id} to not move")
        }

        Gravity.addGravityForces(planets)
        world.step(timeStep, velocityIterations, positionIterations)

        planets.forEach {
            assertTrue(it.worldBody.linearVelocity.length() > 0f, "Expected ${it.id} to move")
        }
        assertTrue(terra.worldBody.linearVelocity.length() < luna.worldBody.linearVelocity.length(),
            "Expected ${terra.id} to move less than ${luna.id}")
    }

    private fun getGravityForceBetweenPlanetSatellite(sx: Float = 0f, sy: Float = 0f): Vec2 {
        val world = World(Vec2())

        val planets = mutableListOf<Planet>()
        val terra = Planet("terra", planets, world, sx, sy, 0f, 0f, 0f, 0f, 100f, 10f,
            texture = TextureEnum.white_pixel)
        val luna = Planet("luna", planets, world, 0f, 0f, 0f, 0f, 0f, 0f, 100f, 10f,
            texture = TextureEnum.white_pixel)

        return Gravity.gravitationalForce(luna, terra)
    }
}
