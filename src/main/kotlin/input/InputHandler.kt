package input

import display.MouseButtonEvent
import display.Window
import game.GamePhaseHandler
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.jbox2d.common.Vec2
import org.lwjgl.glfw.GLFW

class InputHandler(private val gamePhaseHandler: GamePhaseHandler) {

    private val unsubscribe = PublishSubject.create<Boolean>()

    fun init(window: Window) {
        val mouseButtonRelease = PublishSubject.create<Boolean>()
        window.mouseButtonEvent.takeUntil(unsubscribe).subscribe { click ->
            dragRightClick(click, window, mouseButtonRelease)
        }

        getDoubleClick(window)
            .takeUntil(unsubscribe)
            .subscribe { (_, click) -> gamePhaseHandler.doubleLeftClick(window.getCursorPosition(), click) }

        window.mouseScrollEvent.takeUntil(unsubscribe).subscribe {
            gamePhaseHandler.scrollCamera(it.y)
        }

        window.keyboardEvent.takeUntil(unsubscribe).subscribe {
            when (it.action) {
                GLFW.GLFW_PRESS -> {
                    when (it.key) {
                        GLFW.GLFW_KEY_SPACE -> gamePhaseHandler.pauseGame(it)
                        GLFW.GLFW_KEY_LEFT -> gamePhaseHandler.keyPressArrowLeft(it)
                        GLFW.GLFW_KEY_RIGHT -> gamePhaseHandler.keyPressArrowRight(it)
                    }
                }
            }
        }
    }

    private fun getDoubleClick(window: Window): Observable<Pair<Boolean, MouseButtonEvent>> {
        var lastLeftClickTimeStamp = System.currentTimeMillis()
        return window.mouseButtonEvent
            .filter { it.button == GLFW.GLFW_MOUSE_BUTTON_LEFT && it.action == GLFW.GLFW_PRESS }
            .map {
                val isDoubleClick = lastLeftClickTimeStamp > System.currentTimeMillis() - 300
                lastLeftClickTimeStamp = System.currentTimeMillis()
                Pair(isDoubleClick, it)
            }
            .filter { (isDoubleClick, _) -> isDoubleClick }
    }

    private fun dragRightClick(click: MouseButtonEvent, window: Window, mouseButtonRelease: PublishSubject<Boolean>) {
        if (click.button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && click.action == GLFW.GLFW_PRESS) {

            var startLocation: Vec2? = null
            window.cursorPositionEvent.takeUntil(mouseButtonRelease).subscribe { location ->
                location.x *= -1f

                if (startLocation != null) {
                    val movement = startLocation!!.add(location.mul(-1f))
                    gamePhaseHandler.dragMouseRightClick(movement)
                }
                startLocation = location
            }
        } else if (click.button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && click.action == GLFW.GLFW_RELEASE) {
            mouseButtonRelease.onNext(true)
        }
    }

    fun dispose() {
        unsubscribe.onNext(true)
    }

}
