package display.gui

import dI
import display.draw.Drawer
import display.draw.TextureHolder
import display.event.MouseButtonEvent
import display.graphic.Renderer
import display.gui.base.HasDrag
import display.gui.element.GuiButton
import display.gui.element.GuiScroll
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable.never
import io.reactivex.subjects.PublishSubject
import org.jbox2d.common.Vec2
import org.junit.jupiter.api.*
import org.lwjgl.glfw.GLFW
import utility.Common
import utility.Expect.Companion.expect

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

    val mbeInBounds = MouseButtonEvent(0, GLFW.GLFW_PRESS, 0, inBounds)
    val mbeOutOfBounds = MouseButtonEvent(0, GLFW.GLFW_PRESS, 0, outOfBounds)

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

    @DisplayName("handleLeftClick(location, event)")
    @Nested
    inner class HandleLeftClickPress {

        @Test
        fun `when location out of bounds, it does nothing`() {
            val (scroll, kid) = makeScrollAndKid()
            scroll.handleLeftClick(mbeOutOfBounds, never())

            verify(inverse = true) { kid.handleLeftClick(any(), any()) }
        }

        @Test
        fun `when location is on kid, it calls kid elements handleLeftClick`() {
            val (scroll, kid) = makeScrollAndKid()
            every { kid.handleLeftClick(any(), any()) } returns true
            val mbeKid = MouseButtonEvent(0, GLFW.GLFW_PRESS, 0, kid.offset)

            expect(scroll.handleLeftClick(mbeKid, never())).truly

            verify { kid.handleLeftClick(any(), any()) }
        }

        @Test
        fun `when dragged, kid elements offset change`() {
            val scroll = GuiScroll()
            val kid = GuiButton()
            scroll.addKids(listOf(kid) + (0..2).map { GuiButton() })

            val scrollOffsetOld = scroll.offset.clone()
            val kidOffsetOld = kid.offset.clone()
            val location = Vec2(2f, 20f)

            val mouse = PublishSubject.create<MouseButtonEvent>()

            val mbeDrag = MouseButtonEvent(0, GLFW.GLFW_PRESS, 0, scroll.offset)
            scroll.handleLeftClick(mbeDrag, mouse)
            mouse.onNext(MouseButtonEvent(-1, -1, -1, Vec2()))
            mouse.onNext(MouseButtonEvent(-1, -1, -1, location))
            mouse.onNext(MouseButtonEvent(-1, -1, -1, location.mul(2f)))
            mouse.onNext(MouseButtonEvent(0, GLFW.GLFW_RELEASE, 0, location.mul(2f)))
            mouse.onComplete()

            repeat(3) { scroll.update() }

            expect(scroll.offset).same(scrollOffsetOld)
            expect(kid.offset).not.same(kidOffsetOld)
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
