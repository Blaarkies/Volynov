package app

import display.draw.Drawer
import display.draw.TextureHolder
import display.graphic.Renderer
import display.Window
import engine.GameState
import engine.freeBody.FreeBody
import game.GamePhaseHandler
import game.MapGenerator
import input.InputHandler

class AppLogic : IGameLogic {

    private val renderer = Renderer()

    private val gameState = GameState()
    private val textures = TextureHolder()
    private val drawer = Drawer(renderer, textures)
    private val gamePhaseHandler = GamePhaseHandler(gameState, drawer, textures)
    private val inputHandler = InputHandler(gamePhaseHandler)

    @Throws(Exception::class)
    override fun init(window: Window) {
        gameState.init(window)
        renderer.init(gameState.camera)
        textures.init()
        inputHandler.init(window)

        MapGenerator.populateTestMap(gameState, textures)
        gameState.camera.trackFreeBody(gameState.tickables.find { it.id == "terra" } as FreeBody)
    }

    override fun input(window: Window) {
    }

    override fun update(interval: Float) {
        gamePhaseHandler.update()
    }

    override fun render(window: Window) {
        renderer.clear()

        drawer.drawPicture(textures.stars_2k)

        val allFreeBodies = gameState.tickables
        allFreeBodies.forEach { drawer.drawTrail(it) }
        allFreeBodies.forEach { drawer.drawFreeBody(it) }
//        allFreeBodies.forEach { drawDebugForces(it) }
//        drawer.drawGravityCells(gameState.gravityMap, gameState.resolution)
    }

    override fun cleanup() {
        renderer.dispose()
        inputHandler.dispose()
    }
}
