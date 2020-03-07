package app

import display.draw.Drawer
import display.draw.TextureHolder
import display.graphic.Renderer
import display.graphic.Window
import engine.GameState
import game.MapGenerator
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.World
import org.lwjgl.glfw.GLFW

class AppLogic : IGameLogic {

    private var paused = false
    private val renderer = Renderer()

    private val gameState = GameState()
    private val textures = TextureHolder()
    private val drawer = Drawer(renderer, textures)

    private val world: World = World(Vec2(0f, 0f))
    private val timeStep = 1f / 60f
    private val velocityIterations = 8
    private val positionIterations = 3
//    var lastTime = System.currentTimeMillis()

    @Throws(Exception::class)
    override fun init() {
        renderer.init()
        textures.init()

        MapGenerator.populateTestMap(gameState, world, textures)
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

//        drawer.drawGravityCells(gameState.gravityMap, gameState.resolution)
    }

    override fun cleanup() {
        renderer.dispose()
    }
}
