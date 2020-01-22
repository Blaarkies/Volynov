import app.IGameLogic
import display.Window
import display.Renderer
import engine.GameState
import org.lwjgl.glfw.GLFW
import kotlin.concurrent.thread

class AppLogic : IGameLogic {

    private var direction: Int = 0
    private var color: Float = 0f
    private var paused: Boolean = false
    private val renderer: Renderer = Renderer()

    val gameState = GameState()

    init {
        gameState.addPlayer(-350.0, 0.0, 0.0, 0.0, -0.3, .5, "1")
        gameState.addPlanet(.0, .0, .0, 0.0, -0.135, -.3, "J", 325.0, 40.0, 2000.0)
        gameState.addPlanet(-300.0, 0.0, 0.0, 0.0, 0.9, -.5, "B", 325.0, 20.0, 300.0)
    }

    @Throws(Exception::class)
    override fun init() {
        renderer.init()
    }

    override fun input(window: Window) {
        if (window.isKeyPressed(GLFW.GLFW_KEY_SPACE)) {
            paused = !paused
        }

        direction = when {
            window.isKeyPressed(GLFW.GLFW_KEY_UP) -> 1
            window.isKeyPressed(GLFW.GLFW_KEY_DOWN) -> -1
            else -> 0
        }
    }

    override fun update(interval: Float) {
        if (!paused) {
            gameState.tickClock()
        }

        color += direction * 0.01f
        color.coerceIn(0f, 1f)
    }

    override fun render(window: Window) {
        window.setClearColor(color, color, color, 0f)
        renderer.render(window, gameState)
    }

    override fun cleanup() {
        renderer.cleanup()
    }
}
