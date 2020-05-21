package engine.freeBody

import TestConstants.positionIterations
import TestConstants.timeStep
import TestConstants.velocityIterations
import display.draw.TextureEnum
import engine.FreeBodyCallback
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

        val vehicles = mutableListOf<Vehicle>()
        aliceVehicle = Vehicle(vehicles, world, GamePlayer("alice"), 0f, 0f, 0f, 0f, 0f, 0f, 1f, .7f, .5f, .5f,
            TextureEnum.white_pixel)
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
        val planets = mutableListOf<Planet>()
        val terra = Planet("terra", planets, world, 10f, 0f, 0f, 0f, 0f, 0f, 1600f, 4.5f,
            texture = TextureEnum.white_pixel)

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

    @Test
    fun detonation_should_knock_nearby_bodies() {
        val gravityBodies = mutableListOf<FreeBody>()

        val planets = mutableListOf<Planet>()
        Planet("pluto", planets, world, 1f, 1f, 0f, 0f, 0f, 0f, 1f, .1f, texture = TextureEnum.white_pixel)

        val vehicles = mutableListOf<Vehicle>()
        val player = GamePlayer("sputnik")
        Vehicle(vehicles, world, player, 1f, 0f, 0f, 0f, 0f, 0f, 1f, .1f, texture = TextureEnum.white_pixel)

        val particles = mutableListOf<Particle>()
        val warheads = mutableListOf<Warhead>()
        val tickTime = 0f
        Warhead("dud", warheads, world, player, 1f, -1f, 0f, 0f, 0f, 0f, 1f, .1f,
            onCollision = { self, impacted ->
                (self as Warhead).detonate(world, tickTime, warheads, particles, vehicles, gravityBodies, impacted)
            },
            createdAt = tickTime)

        val boom = Warhead("boom", warheads, world, player, 0f, 0f, 0f, 0f, 0f, 0f, 1f, .1f,
            onCollision = { self, impacted ->
                (self as Warhead).detonate(world, tickTime, warheads, particles, vehicles, gravityBodies, impacted)
            },
            createdAt = tickTime)

        gravityBodies.addAll(planets + vehicles + warheads)

        boom.freeBodyCallback.callback(boom, boom.worldBody)
        world.step(timeStep, velocityIterations, positionIterations)

        gravityBodies.forEach {
            assertTrue(it.worldBody.linearVelocity.length() > .1f,
                "Expected ${it.id} to be knocked away due to explosion")
        }
    }
}
