package app

import display.draw.Drawer
import display.draw.TextureHolder
import display.graphic.Renderer
import display.Window
import engine.GameState
import game.GamePhaseHandler
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
        gamePhaseHandler.init(window)
        inputHandler.init(window)
    }

    override fun update(interval: Float) {
        gamePhaseHandler.update()
    }

    override fun render(window: Window) {
        renderer.clear()
        gamePhaseHandler.render()
    }

    override fun cleanup() {
        renderer.dispose()
        inputHandler.dispose()
    }
}
