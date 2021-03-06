package engine.freeBody

import TestConstants.positionIterations
import TestConstants.timeStep
import TestConstants.velocityIterations
import dI
import display.draw.Drawer
import display.draw.TextureEnum
import engine.gameState.GameState
import engine.physics.Gravity
import game.GamePlayer
import game.GamePlayerType
import io.mockk.mockk
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.World
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import utility.Common.Pi
import kotlin.math.PI
import kotlin.math.absoluteValue

internal class FreeBodyTest {

    private val momentum = 1f
    private val direction = Pi

    private lateinit var world: World
    private lateinit var aliceVehicle: Vehicle
    private lateinit var location: Vec2
    private lateinit var velocity: Vec2
    private var rotation: Float = 0f

    init {
        val gameState: GameState = mockk(relaxed = true)
        dI.gameState = gameState
    }

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
    fun `step when vehicles created, they do not move in idle world`() {
        listOf(location.x, location.y, velocity.x, velocity.y, rotation)
            .forEach { assertTrue(it == 0f) }

        world.step(timeStep, velocityIterations, positionIterations)

        listOf(location.x, location.y, velocity.x, velocity.y, rotation)
            .forEach { assertTrue(it == 0f) }
    }

    @Test
    fun `knock when vehicle is knocked, it should recoil`() {
        aliceVehicle.knock(momentum, direction)

        world.step(timeStep, velocityIterations, positionIterations)
        assertTrue(velocity.x < 0f)
        assertTrue(velocity.y.absoluteValue < .0001f)
        assertTrue(rotation == 0f)
    }

    @Test
    fun `knock when vehicle is knocked in a gravity field, it should recoil`() {
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
    fun `detonate when warhead is near other bodies, it should knock nearby bodies`() {
        val gravityBodies = mutableListOf<FreeBody>()

        val planets = mutableListOf<Planet>()
        Planet("pluto", planets, world, 1f, 1f, 0f, 0f, 0f, 0f, 1f, .1f, texture = TextureEnum.white_pixel)

        val vehicles = mutableListOf<Vehicle>()
        val player = GamePlayer("sputnik", GamePlayerType.CLONE)
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
