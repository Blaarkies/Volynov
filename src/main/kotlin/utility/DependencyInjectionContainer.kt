import display.Window
import display.draw.Drawer
import display.draw.TextureHolder
import display.graphic.Renderer
import display.gui.GuiController
import engine.gameState.GameState
import game.GamePhaseHandler
import input.CameraView
import input.InputHandler
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class DependencyInjectionContainer {

    lateinit var window: Window

    lateinit var cameraView: CameraView
    lateinit var renderer: Renderer
    lateinit var textures: TextureHolder
    lateinit var drawer: Drawer
    lateinit var gameState: GameState
    lateinit var guiController: GuiController
    lateinit var gamePhaseHandler: GamePhaseHandler
    lateinit var inputHandler: InputHandler
    var isDebugMode = false
    private val isDone = PublishSubject.create<Unit>()
    val whenDone: Observable<Unit>

    init {
        whenDone = isDone.take(1).map { Unit }
    }

    fun init() {
        cameraView = CameraView()
        renderer = Renderer()
        textures = TextureHolder()
        drawer = Drawer()
        gameState = GameState()
        guiController = GuiController()
        gamePhaseHandler = GamePhaseHandler()
        inputHandler = InputHandler()

        isDone.onNext(Unit)
    }
}
