package input

import dI
import io.reactivex.Observable.just
import io.reactivex.Observable.merge
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.flow.merge
import org.lwjgl.glfw.GLFW

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
        val mouseDownEvent = window.mouseButtonEvent.filter { it.isPress }
        val mouseUpEvent = window.mouseButtonEvent.filter { it.isRelease }

        mouseDownEvent.filter { it.isLeft }
            .takeUntil(unsubscribe)
            .subscribe { clickPress ->
                val clickRelease =
                    mouseUpEvent.filter { it.isLeft } // TODO: release any button to cancel the other button?

                val event = merge(just(clickPress.clone()), window.cursorPositionEvent, clickRelease)
                    .takeUntil { it.isRelease }
                gamePhaseHandler.eventLeftClick(clickPress.clone(), event)
            }

        mouseDownEvent.filter { it.isRight }
            .takeUntil(unsubscribe)
            .subscribe { clickPress ->
                val clickRelease = mouseUpEvent.filter { it.isRight }
                val event = merge(just(clickPress.clone()), window.cursorPositionEvent, clickRelease)
                    .takeUntil { it.isRelease }
                gamePhaseHandler.eventRightClick(clickPress.clone(), event)
            }
    }

    private fun setupMouseMove() {
        window.cursorPositionEvent.takeUntil(unsubscribe).subscribe {
            gamePhaseHandler.moveMouse(it.location)
        }
    }

    private fun setupKeyboard() {
        window.keyboardEvent
            .filter { it.isPress }
            .takeUntil(unsubscribe)
            .subscribe { keyPress ->
                val keyRelease = window.keyboardEvent.filter { it.isRelease && it.key == keyPress.key }
                val event = merge(just(keyPress), keyRelease).take(2)
                gamePhaseHandler.eventKeyboardKey(keyPress, event)
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
