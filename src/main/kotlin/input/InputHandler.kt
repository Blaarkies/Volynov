package input

import dI
import display.Window
import display.events.MouseButtonEvent
import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.Observable.merge
import io.reactivex.subjects.PublishSubject
import org.jbox2d.common.Vec2
import org.lwjgl.glfw.GLFW
import java.util.concurrent.TimeUnit

class InputHandler {

    private val window = dI.window
    private val gamePhaseHandler = dI.gamePhaseHandler

    private val unsubscribe = PublishSubject.create<Boolean>()

    val keyboardEvent = window.keyboardEvent
    val mouseButtonEvent = window.mouseButtonEvent
    val textInputEvent = window.textInputEvent

    init {
        setupDoubleLeftClick()
        setupMouseScroll()
        setupKeyboard()
        setupMouseMove()
        setupMouseClicks()
    }

    private fun setupMouseClicks() {
        window.mouseButtonEvent
            .filter { it.action == GLFW.GLFW_PRESS && it.button == GLFW.GLFW_MOUSE_BUTTON_LEFT }
            .takeUntil(unsubscribe)
            .subscribe { clickPress ->
                val clickRelease = window.mouseButtonEvent
                    .filter { it.action == GLFW.GLFW_RELEASE && it.button == GLFW.GLFW_MOUSE_BUTTON_LEFT }

                val event = merge(just(clickPress.clone()), window.cursorPositionEvent, clickRelease)
                    .takeUntil { it.action == GLFW.GLFW_RELEASE }
                gamePhaseHandler.eventLeftClick(clickPress.clone(), event)
            }

        window.mouseButtonEvent
            .filter { it.action == GLFW.GLFW_PRESS && it.button == GLFW.GLFW_MOUSE_BUTTON_RIGHT }
            .takeUntil(unsubscribe)
            .subscribe { clickPress ->
                val clickRelease = window.mouseButtonEvent
                    .filter { it.action == GLFW.GLFW_RELEASE && it.button == GLFW.GLFW_MOUSE_BUTTON_RIGHT }
                val event = merge(just(clickPress.clone()), window.cursorPositionEvent, clickRelease)
                    .takeUntil { it.action == GLFW.GLFW_RELEASE }
                gamePhaseHandler.eventRightClick(clickPress.clone(), event)
            }
    }

    private fun setupMouseMove() {
        window.cursorPositionEvent.takeUntil(unsubscribe).subscribe {
            gamePhaseHandler.moveMouse(it.location)
        }
    }

    private fun setupKeyboard() {
        window.keyboardEvent.takeUntil(unsubscribe).subscribe {
            when (it.action) {
                GLFW.GLFW_PRESS -> {
                    when (it.key) {
                        // GLFW.GLFW_KEY_SPACE -> gamePhaseHandler.pauseGame(it)
                        GLFW.GLFW_KEY_LEFT -> gamePhaseHandler.keyPressArrowLeft(it)
                        GLFW.GLFW_KEY_RIGHT -> gamePhaseHandler.keyPressArrowRight(it)
                        GLFW.GLFW_KEY_ESCAPE -> gamePhaseHandler.keyPressEscape(it)
                        GLFW.GLFW_KEY_BACKSPACE -> gamePhaseHandler.keyPressBackspace(it)
                        GLFW.GLFW_KEY_ENTER -> gamePhaseHandler.keyPressEnter(it)
                    }
                }
            }
        }
    }

    private fun setupMouseScroll() {
        window.mouseScrollEvent.takeUntil(unsubscribe)
            .subscribe { gamePhaseHandler.scrollMouse(it) }
    }

    private fun setupDoubleLeftClick() {
        var lastLeftClickTimeStamp = System.currentTimeMillis()
        window.mouseButtonEvent
            .filter { it.button == GLFW.GLFW_MOUSE_BUTTON_LEFT && it.action == GLFW.GLFW_PRESS }
            .map {
                val isDoubleClick = lastLeftClickTimeStamp > System.currentTimeMillis() - 300
                lastLeftClickTimeStamp = System.currentTimeMillis()
                Pair(isDoubleClick, it)
            }
            .filter { (isDoubleClick, _) -> isDoubleClick }
            .takeUntil(unsubscribe)
            .subscribe { (_, click) -> gamePhaseHandler.doubleLeftClick(click.location) }
    }

    fun dispose() {
        unsubscribe.onNext(true)
    }

}
