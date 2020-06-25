package display.gui

import dI
import display.graphic.Renderer
import io.mockk.mockk
import io.mockk.verify
import org.jbox2d.common.Vec2
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
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

    @DisplayName("handleLeftClickPress(location)")
    @Nested
    inner class HandleLeftClickPress {

        private var wasClicked: Boolean = false
        private lateinit var button: GuiButton

        @BeforeEach
        fun setup() {
            wasClicked = false
            button = GuiButton(baseOffset(), baseScale(), onClick = { wasClicked = true })
        }

        @Test
        fun `when location is out of bounds, nothing happens`() {
            button.handleLeftClickPress(outOfBounds)

            assert(!wasClicked)
            verifyIdlePhase(button)
        }

        @Test
        fun `when location is in bounds, button is pressed and not clicked`() {
            button.handleLeftClickPress(inBounds)

            assert(!wasClicked)
            verifyActivePhase(button)
        }
    }

    @DisplayName("handleLeftClickRelease(location)")
    @Nested
    inner class HandleLeftClickRelease {

        private var wasClicked: Boolean = false
        private lateinit var button: GuiButton

        @BeforeEach
        fun setup() {
            wasClicked = false
            button = GuiButton(baseOffset(), baseScale(), onClick = { wasClicked = true })
        }

        @Test
        fun `when button is not pressed, nothing happens`() {
            val lastPhase = button.currentPhase
            button.handleLeftClickRelease(inBounds)
            assert(!wasClicked)
            assert(lastPhase == button.currentPhase)
        }

        @Test
        fun `when button is pressed but released out of bounds, onClick does not fire and isPressed resets`() {
            button.handleLeftClickPress(inBounds)
            button.handleLeftClickRelease(outOfBounds)

            assert(!wasClicked)
            verifyIdlePhase(button)
        }

        @Test
        fun `when button is pressed and released in bounds, onClick fires and isPressed resets`() {
            button.handleLeftClickPress(inBounds)
            button.handleLeftClickRelease(inBounds)

            assert(wasClicked)
            verifyIdlePhase(button)
        }
    }

    private fun verifyIdlePhase(element: GuiElement) {
        expect(element.currentPhase).same(GuiElementPhases.IDLE)
    }

    private fun verifyActivePhase(element: GuiElement) {
        expect(element.currentPhase).same(GuiElementPhases.ACTIVE)
    }

    private fun verifyHoverPhase(element: GuiElement) {
        expect(element.currentPhase).same(GuiElementPhases.HOVER)
    }

}
