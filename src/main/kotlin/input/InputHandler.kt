package input

import display.events.MouseButtonEvent
import display.Window
import display.events.MouseScrollEvent
import game.GamePhaseHandler
import io.reactivex.subjects.PublishSubject
import org.jbox2d.common.Vec2
import org.lwjgl.glfw.GLFW

class InputHandler(private val gamePhaseHandler: GamePhaseHandler) {

    private val unsubscribe = PublishSubject.create<Boolean>()

    fun init(window: Window) {
        setupDragClick(window)
        setupDoubleLeftClick(window)
        setupMouseScroll(window)
        setupKeyboard(window)
        setupMouseMove(window)
        setupMouseClicks(window)
        setupTextInput(window)
    }

    private fun setupTextInput(window: Window) {
        window.textInputEvent.takeUntil(unsubscribe).subscribe {
            gamePhaseHandler.inputText(it)
        }
    }

    private fun setupMouseClicks(window: Window) {
        window.mouseButtonEvent.takeUntil(unsubscribe).subscribe {
            if (it.action == GLFW.GLFW_PRESS) {
                when (it.button) {
                    GLFW.GLFW_MOUSE_BUTTON_LEFT -> gamePhaseHandler.leftClickMouse(it)
                }
            }
        }
    }

    private fun setupMouseMove(window: Window) {
        window.cursorPositionEvent.takeUntil(unsubscribe).subscribe {
            gamePhaseHandler.moveMouse(it)
        }
    }

    private fun setupKeyboard(window: Window) {
        window.keyboardEvent.takeUntil(unsubscribe).subscribe {
            when (it.action) {
                GLFW.GLFW_PRESS -> {
                    when (it.key) {
                        //                        GLFW.GLFW_KEY_SPACE -> gamePhaseHandler.pauseGame(it)
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

    private fun setupMouseScroll(window: Window) {
        window.mouseScrollEvent.takeUntil(unsubscribe)
            .subscribe { gamePhaseHandler.scrollMouse(it) }
    }

    private fun setupDoubleLeftClick(window: Window) {
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
            .subscribe { (_, click) -> gamePhaseHandler.doubleLeftClick(window.getCursorPosition()) }
    }

    private fun setupDragClick(window: Window) {
        val mouseButtonLeftRelease = PublishSubject.create<Boolean>()
        val mouseButtonRightRelease = PublishSubject.create<Boolean>()
        window.mouseButtonEvent.takeUntil(unsubscribe)
            .subscribe { click -> dragClick(click, window, mouseButtonLeftRelease, mouseButtonRightRelease) }
    }

    private fun dragClick(click: MouseButtonEvent,
                          window: Window,
                          mouseButtonLeftRelease: PublishSubject<Boolean>,
                          mouseButtonRightRelease: PublishSubject<Boolean>
    ) {
        if (click.button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            when (click.action) {
                GLFW.GLFW_PRESS -> {
                    window.cursorPositionEvent.takeUntil(mouseButtonLeftRelease)
                        .subscribe {
                            handleMouseMovement(click.location, it) { movement ->
                                gamePhaseHandler.dragMouseLeftClick(click.location, movement)
                            }
                        }
                }
                GLFW.GLFW_RELEASE -> mouseButtonLeftRelease.onNext(true)
            }
        }

        if (click.button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            when (click.action) {
                GLFW.GLFW_PRESS -> {
                    window.cursorPositionEvent.takeUntil(mouseButtonRightRelease)
                        .subscribe {
                            handleMouseMovement(click.location, it) { movement ->
                                gamePhaseHandler.dragMouseRightClick(movement)
                            }
                        }
                }
                GLFW.GLFW_RELEASE -> mouseButtonRightRelease.onNext(true)
            }
        }
    }

    private fun handleMouseMovement(startLocation: Vec2, location: Vec2, callback: (Vec2) -> Unit) {
        val movement = startLocation.add(location.mul(-1f)).also { it.x *= -1f }
        callback(movement)
        startLocation.set(location)
    }

    fun dispose() {
        unsubscribe.onNext(true)
    }

}
