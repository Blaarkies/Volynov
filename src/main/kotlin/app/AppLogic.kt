import app.IGameLogic
import display.Window
import display.Renderer
import engine.GameState
import org.lwjgl.glfw.GLFW

class AppLogic : IGameLogic {

    private var direction: Int = 0
    private var color: Float = 0.0f
    private val renderer: Renderer = Renderer()

    val gameState = GameState()

    init {
        gameState.addPlayer(.0, 200.0, 0.0, .3, .0, 0.1, "1")
        gameState.addPlanet(.0, .0, 0.0, 0.0, -0.0, -0.1, "A", 325.0, 30.0, 1000.0)
//        gameState.addPlanet(-300.0, -300.0, 0.0, -0.7, 0.7, 0.1, "B", 325.0, 25.0, 70.0)
    }

    @Throws(Exception::class)
    override fun init() {
        renderer.init()
    }

    override fun input(window: Window) {
        direction = when {
            window.isKeyPressed(GLFW.GLFW_KEY_UP) -> 1
            window.isKeyPressed(GLFW.GLFW_KEY_DOWN) -> -1
            else -> 0
        }
    }

    override fun update(interval: Float) {
        gameState.tickClock()

        color += direction * 0.01f
        color.coerceIn(0.0f, 1.0f)
    }

    override fun render(window: Window) {
        window.setClearColor(color, color, color, 0.0f)
        renderer.render(window, gameState)
    }

    override fun cleanup() {
        renderer.cleanup()
    }
}
