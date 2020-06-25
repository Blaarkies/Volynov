import display.Window
import display.draw.Drawer
import display.draw.TextureHolder
import display.graphic.Renderer
import engine.gameState.GameState
import game.GamePhaseHandler
import input.CameraView
import input.InputHandler

class DependencyInjectionContainer {

    lateinit var window: Window

    lateinit var cameraView: CameraView
    lateinit var renderer: Renderer
    lateinit var textures: TextureHolder
    lateinit var drawer: Drawer
    lateinit var gameState: GameState
    lateinit var gamePhaseHandler: GamePhaseHandler
    lateinit var inputHandler: InputHandler
    var isDebugMode = false

    fun init() {
        cameraView = CameraView()
        renderer = Renderer()
        textures = TextureHolder()
        drawer = Drawer()
        gameState = GameState()
        gamePhaseHandler = GamePhaseHandler()
        inputHandler = InputHandler()
    }
}
