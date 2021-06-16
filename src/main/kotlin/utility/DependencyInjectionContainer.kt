import display.Window
import display.draw.Drawer
import display.draw.ModelHolder
import display.draw.TextureHolder
import display.graphic.NewRenderer
import display.graphic.OldRenderer
import display.gui.GuiController
import engine.gameState.GameState
import game.GamePhaseHandler
import input.CameraView
import input.InputHandler
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class DependencyInjectionContainer {

    lateinit var window: Window

    lateinit var cameraView: CameraView
    lateinit var oldRenderer: OldRenderer
    lateinit var newRenderer: NewRenderer
    lateinit var textures: TextureHolder
    lateinit var models: ModelHolder
    lateinit var drawer: Drawer
    lateinit var gameState: GameState
    lateinit var guiController: GuiController
    lateinit var gamePhaseHandler: GamePhaseHandler
    lateinit var inputHandler: InputHandler

    var isDebugMode = false
    private val isDone = PublishSubject.create<Unit>()
    val whenDone: Observable<Unit> = isDone.take(1)

    fun init() {
        cameraView = CameraView()
        oldRenderer = OldRenderer()
        newRenderer = NewRenderer()
        textures = TextureHolder()
        models = ModelHolder()
        drawer = Drawer()
        gameState = GameState()
        guiController = GuiController()
        gamePhaseHandler = GamePhaseHandler()
        inputHandler = InputHandler()

        isDone.onNext(Unit)
    }
}
