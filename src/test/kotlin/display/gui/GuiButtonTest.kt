package display.gui

import dI
import display.event.MouseButtonEvent
import display.graphic.Renderer
import display.gui.base.GuiElement
import display.gui.base.GuiElementPhase
import display.gui.element.GuiButton
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable.never
import io.reactivex.subjects.PublishSubject
import org.jbox2d.common.Vec2
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.lwjgl.glfw.GLFW
import utility.Common
import utility.Expect.Companion.expect

internal class GuiButtonTest {

    init {
        val renderer: Renderer = mockk(relaxed = true)
        dI.renderer = renderer
    }

    val baseOffset = { Vec2() }
    val baseScale = { Common.makeVec2(1) }

    val inBounds = Vec2(0f, 0f)
    val outOfBounds = Vec2(-2f, 0f)

    val mbeInBounds = MouseButtonEvent(0, GLFW.GLFW_PRESS, 0, inBounds)
    val mbeOutOfBounds = MouseButtonEvent(0, GLFW.GLFW_PRESS, 0, outOfBounds)

    @Test
    fun `when constructed idle, button phase is correct`() {
        val button = GuiButton(baseOffset(), baseScale())
        verifyIdlePhase(button)
        verify(inverse = true) {
            dI.renderer.drawShape(any())
            dI.renderer.drawStrip(any())
            dI.renderer.drawText(any(), any(), any(), any())
        }
    }

    @DisplayName("handleLeftClick(location, event)")
    @Nested
    inner class HandleLeftClick {

        private var wasClicked: Boolean = false
        private lateinit var button: GuiButton

        @BeforeEach
        fun setup() {
            wasClicked = false
            button = GuiButton(baseOffset(), baseScale(),
                onClick = { wasClicked = true })
        }

        @Test
        fun `when location is out of bounds, nothing happens`() {
            button.handleLeftClick(mbeOutOfBounds, never())

            expect(wasClicked).falsy
            verifyIdlePhase(button)
        }

        @Test
        fun `when location is in bounds, button is pressed and not clicked`() {
            button.handleLeftClick(mbeInBounds, never())

            expect(wasClicked).falsy
            verifyActivePhase(button)
        }

        @Test
        fun `when button is not pressed, nothing happens`() {
            val mouse = PublishSubject.create<MouseButtonEvent>()

            val lastPhase = button.currentPhase
            button.handleLeftClick(mbeOutOfBounds, mouse)
            mouse.onNext(MouseButtonEvent(-1, -1, -1, outOfBounds))
            mouse.onNext(MouseButtonEvent(0, GLFW.GLFW_RELEASE, 0, outOfBounds))
            mouse.onComplete()

            expect(wasClicked).falsy
            assert(lastPhase == button.currentPhase)
        }

        @Test
        fun `when button is pressed but released out of bounds, no onClick call`() {
            val mouse = PublishSubject.create<MouseButtonEvent>()

            button.handleLeftClick(mbeInBounds, mouse)
            mouse.onNext(MouseButtonEvent(-1, -1, -1, outOfBounds))
            mouse.onNext(MouseButtonEvent(0, GLFW.GLFW_RELEASE, 0, outOfBounds))
            mouse.onComplete()

            expect(wasClicked).falsy
            verifyIdlePhase(button)
        }

        @Test
        fun `when button is pressed and released in bounds, calls onClick`() {
            val mouse = PublishSubject.create<MouseButtonEvent>()

            button.handleLeftClick(mbeInBounds, mouse)
            mouse.onNext(MouseButtonEvent(-1, -1, -1, inBounds))
            mouse.onNext(MouseButtonEvent(0, GLFW.GLFW_RELEASE, 0, inBounds))
            mouse.onComplete()

            expect(wasClicked).truly
            verifyIdlePhase(button)
        }
    }

    private fun verifyIdlePhase(element: GuiElement) {
        expect(element.currentPhase).same(GuiElementPhase.IDLE)
    }

    private fun verifyActivePhase(element: GuiElement) {
        expect(element.currentPhase).same(GuiElementPhase.ACTIVE)
    }

    private fun verifyHoverPhase(element: GuiElement) {
        expect(element.currentPhase).same(GuiElementPhase.HOVER)
    }

}
