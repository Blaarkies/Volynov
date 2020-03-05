package app

import display.draw.Drawer
import display.graphic.*
import engine.GameState
import engine.freeBody.Planet
import engine.freeBody.Vehicle
import engine.motion.Director
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.World
import org.lwjgl.glfw.GLFW
import kotlin.math.*

class AppLogic : IGameLogic {

    private var paused = false
    private val renderer = Renderer()

    private val gameState = GameState()
    private val drawer = Drawer(renderer)

    private val world: World = World(Vec2(0f, 0f))
    private val timeStep = 1f / 60f
    private val velocityIterations = 8
    private val positionIterations = 3
//    var lastTime = System.currentTimeMillis()

    init {
        val terra = Planet.create(world, "terra", 0f, 0f, 0f, 0f, 0f, .5f, 1800f, 90f, .3f)
        val luna = Planet.create(world, "luna", 500f, 0f, 0f, 0f, -350f, -2f, 100f, 25f, .5f)
        val alice = Vehicle.create(world, "alice", 500f, -50f, 0f, -110f, 100f, 0f, 3f)
        gameState.vehicles.add(alice)
        gameState.planets.addAll(listOf(terra, luna))
        gameState.planets.addAll((0..700)
            .withIndex()
            .map { (i, _) ->
                val ratio = (2 * PI * 0.07 * i).toFloat()
                val radius = 300
                floatArrayOf(
                    (i * .4f + radius) * cos(ratio),
                    (i * .4f + radius) * sin(ratio),
                    i.toFloat()
                )
            }
            .map {
                val direction = Director.getDirection(-it[0], -it[1]) + PI * .5f
                val speed = 400f
                Planet.create(
                    world, "${it[2].toInt()}", it[0], it[1], 0f,
                    cos(direction).toFloat() * speed,
                    sin(direction).toFloat() * speed,
                    .5f, 0.3f * it[2].rem(6f), 4f + it[2].rem(6f)
                )
            })
    }

    @Throws(Exception::class)
    override fun init() {
        renderer.init()
        drawer.init()
    }

    override fun input(window: Window) {
        if (window.isKeyPressed(GLFW.GLFW_KEY_SPACE)) {
            paused = !paused
        }
    }

    override fun update(interval: Float) {
        if (!paused) {
            gameState.tickClock(world, timeStep, velocityIterations, positionIterations)
        }

    }

    override fun render(window: Window) {
        renderer.clear()

        val allFreeBodies = gameState.planets.union(gameState.vehicles)
        allFreeBodies.forEach { drawer.drawTrail(it) }
        allFreeBodies.forEach { drawer.drawFreeBody(it) }
//        allFreeBodies.forEach { drawDebugForces(it) }

        drawer.drawGravityCells(gameState.gravityMap, gameState.resolution)
    }

    override fun cleanup() {
        renderer.dispose()
    }
}
