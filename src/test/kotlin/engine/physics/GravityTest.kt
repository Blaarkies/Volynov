package engine.physics

import display.draw.TextureConfig
import display.draw.TextureEnum
import display.graphic.Texture
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

//    @Test
//    fun in_binary_system_the_massive_body_moves_less() {
//    }

    private fun getGravityForceBetweenPlanetSatellite(sx: Float = 0f, sy: Float = 0f): Vec2 {
        val world = World(Vec2(0f, 0f))

        val terra = Planet.create(world, "terra", sx, sy, 0f, 0f, 0f, 0f, 100f, 10f,
            textureConfig = TextureConfig(TextureEnum.white_pixel))
        val luna = Planet.create(world, "luna", 0f, 0f, 0f, 0f, 0f, 0f, 100f, 10f,
            textureConfig = TextureConfig(TextureEnum.white_pixel))

        return Gravity.gravitationalForce(luna, terra)
    }
}
