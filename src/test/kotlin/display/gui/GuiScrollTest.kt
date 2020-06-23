package display.gui

import dI
import display.draw.Drawer
import display.draw.TextureHolder
import display.graphic.Renderer
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jbox2d.common.Vec2
import org.junit.jupiter.api.*
import utility.Common

internal class GuiScrollTest {

    init {
        val drawer: Drawer = mockk(relaxed = true)
        dI.drawer = drawer
        val renderer: Renderer = mockk(relaxed = true)
        dI.renderer = renderer
        val textures: TextureHolder = mockk(relaxed = true)
        dI.textures = textures
    }

    val baseOffset = { Vec2() }
    val baseScale = { Common.makeVec2(20f) }

    val inBounds = Vec2(0f, 0f)
    val outOfBounds = Vec2(-100f, 0f)
    val movement = Vec2(0f, 1f)

    @Test
    fun `when constructed idle, scroll renders a border`() {
        val scroll = makeScroll()
        scroll.render(null)

        verify { dI.renderer.drawStrip(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `when update, it calls kid elements update`() {
        val (scroll, kid) = makeScrollAndKid()
        scroll.update()

        verify { kid.update() }
    }

    @TestFactory
    fun `calculateNewOffsets`(): List<DynamicTest> {
        return listOf(
            Pair("addOffset", { element: GuiScroll, offset: Vec2 -> element.addOffset(offset) }),
            Pair("updateOffset", { element: GuiScroll, offset: Vec2 -> element.updateOffset(offset) }))
            .flatMap { (name, method) ->
                listOf(
                    DynamicTest.dynamicTest("when $name, it calls kid elements updateOffset ") {
                        val (scroll, kid) = makeScrollAndKid()
                        val kidOffsetOld = Vec2(0f, 0f)
                        kid.updateOffset(kidOffsetOld)
                        val scrollMovement = Vec2(1f, 0f)
                        method(scroll, scrollMovement)

                        verify { kid.updateOffset(any()) }
                    }
                )
            }
    }

    @Test
    fun `when handleHover in bounds, it calls kid elements handleHover`() {
        val (scroll, kid) = makeScrollAndKid()
        scroll.handleHover(inBounds)

        verify { kid.handleHover(inBounds) }
    }

    @Test
    fun `when handleHover out of bounds, it does not call kid elements handleHover`() {
        val (scroll, kid) = makeScrollAndKid()
        scroll.handleHover(outOfBounds)

        verify(inverse = true) { kid.handleHover(outOfBounds) }
    }

    @DisplayName("handleLeftClickPress(location)")
    @Nested
    inner class HandleLeftClickPress {

        @Test
        fun `when location out of bounds, it does nothing`() {
            val (scroll, kid) = makeScrollAndKid()
            assert(!scroll.handleLeftClickPress(outOfBounds))

            verify(inverse = true) { kid.handleLeftClickPress(outOfBounds) }
        }

        @Test
        fun `when location is on kid, it calls kid elements handleLeftClickPress`() {
            val (scroll, kid) = makeScrollAndKid()

            every { kid.handleLeftClickPress(any()) } returns true
            assert(scroll.handleLeftClickPress(kid.offset))

            verify { kid.handleLeftClickPress(kid.offset) }
        }

        @Test
        fun `when location in bounds, it sets scroll isPressed`() {
            val scroll = makeScroll()

            assert(scroll.handleLeftClickPress(inBounds))
        }
    }

    @DisplayName("handleLeftClickRelease(location)")
    @Nested
    inner class HandleLeftClickRelease {

        @Test
        fun `when location out of bounds, it calls kid elements handleLeftClickPress`() {
            val (scroll, kid) = makeScrollAndKid()
            every { kid.handleLeftClickRelease(any()) } returns true

            assert(scroll.handleLeftClickRelease(outOfBounds))

            verify { kid.handleLeftClickRelease(outOfBounds) }
        }

        @Test
        fun `when no kid elements, it returns false`() {
            val scroll = makeScroll()

            assert(!scroll.handleLeftClickRelease(outOfBounds))
        }
    }

    @DisplayName("handleLeftClickDrag(location, movement)")
    @Nested
    inner class HandleLeftClickDrag {

        @Test
        fun `when scroll accepts handleLeftClickDrag, it resets the isPressed on kids`() {
            val (scroll, kid) = makeScrollAndKid()
            every { kid.handleLeftClickDrag(any(), any()) } returns true

            scroll.handleLeftClickPress(kid.offset)
            assert(scroll.handleLeftClickDrag(kid.offset, movement))

            verify { kid.handleLeftClickRelease(any()) }
        }

    }

    private fun makeScroll(): GuiScroll =
        GuiScroll(baseOffset(), baseScale())

    private fun makeScrollAndKid(): Pair<GuiScroll, HasDrag> {
        val kid: HasDrag = mockk(relaxed = true)
        kid.updateScale(baseScale())
        val scroll = makeScroll().also { it.addKid(kid) }

        return Pair(scroll, kid)
    }

}
