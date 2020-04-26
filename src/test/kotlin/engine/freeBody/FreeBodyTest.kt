package engine.freeBody

import display.draw.TextureConfig
import display.draw.TextureEnum
import engine.physics.Gravity
import game.GamePlayer
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.World
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.math.PI
import kotlin.math.absoluteValue

internal class FreeBodyTest {

    val timeStep = 1f / 60f
    val velocityIterations = 8
    val positionIterations = 3

    val momentum = 1f
    val direction = PI.toFloat()

    lateinit var world: World
    lateinit var aliceVehicle: Vehicle
    lateinit var location: Vec2
    lateinit var velocity: Vec2
    var rotation: Float = 0f

    @BeforeEach
    fun setUp() {
        world = World(Vec2())
        aliceVehicle = Vehicle.create(world, GamePlayer("alice"), 0f, 0f, 0f, 0f, 0f, 0f, 1f, .7f, .5f, .5f,
            TextureConfig(TextureEnum.white_pixel))
        location = aliceVehicle.worldBody.position
        velocity = aliceVehicle.worldBody.linearVelocity
        rotation = aliceVehicle.worldBody.angularVelocity
    }

    @Test
    fun created_vehicle_should_not_move() {
        listOf(location.x, location.y, velocity.x, velocity.y, rotation)
            .forEach { assertTrue(it == 0f) }

        world.step(timeStep, velocityIterations, positionIterations)

        listOf(location.x, location.y, velocity.x, velocity.y, rotation)
            .forEach { assertTrue(it == 0f) }
    }

    @Test
    fun knocked_vehicle_should_recoil() {
        aliceVehicle.knock(momentum, direction)

        world.step(timeStep, velocityIterations, positionIterations)
        assertTrue(velocity.x < 0f)
        assertTrue(velocity.y.absoluteValue < .0001f)
        assertTrue(rotation == 0f)
    }

    @Test
    fun knocked_vehicle_should_recoil_with_gravity_field() {
        val terra = Planet.create(world, "terra", 10f, 0f, 0f, 0f, 0f, 0f, 1600f, 4.5f,
            textureConfig = TextureConfig(TextureEnum.white_pixel))
        val gravityBodies = listOf(aliceVehicle, terra)

        world.step(timeStep, velocityIterations, positionIterations)
        Gravity.addGravityForces(gravityBodies)

        aliceVehicle.knock(momentum, direction)

        world.step(timeStep, velocityIterations, positionIterations)
        Gravity.addGravityForces(gravityBodies)

        assertTrue(velocity.x < 0f)
        assertTrue(velocity.y.absoluteValue < .0001f)
        assertTrue(rotation == 0f)
    }
}
