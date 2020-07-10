package display.gui

import dI
import display.draw.Drawer
import display.draw.TextureHolder
import display.graphic.Renderer
import display.gui.base.GuiElement
import display.gui.base.HasDrag
import display.gui.base.HasScroll
import display.gui.elements.GuiPanel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jbox2d.common.Vec2
import org.junit.jupiter.api.*
import utility.Common

internal class GuiPanelTest {

    init {
        val drawer: Drawer = mockk(relaxed = true)
        dI.drawer = drawer
        val renderer: Renderer = mockk(relaxed = true)
        dI.renderer = renderer
        val textures: TextureHolder = mockk(relaxed = true)
        dI.textures = textures
    }

    val baseOffset = { Vec2() }
    val baseScale = { Common.makeVec2(1) }

    val inBounds = Vec2(0f, 0f)
    val outOfBounds = Vec2(-2000f, 0f)
    val movement = Vec2(0f, 1f)

    @Test
    fun `when constructed as not draggable, it has no visual drag handle`() {
        val panel = makePanel()
        panel.render(null)

        verify(inverse = true) { dI.renderer.drawStrip(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `when constructed as draggable, it has a visual drag handle`() {
        val panel = makePanel(true)
        panel.render(null)

        verify { dI.renderer.drawStrip(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `when update, it calls kid elements update`() {
        val (panel, kid) = makePanelAndKid()
        panel.update()

        verify { kid.update() }
    }

    @TestFactory
    fun `calculateNewOffsets and calculateDraggableRegion`(): List<DynamicTest> {
        return listOf(
            Pair("addOffset", { element: GuiPanel, offset: Vec2 -> element.addOffset(offset) }),
            Pair("updateOffset", { element: GuiPanel, offset: Vec2 -> element.updateOffset(offset) }))
            .flatMap { (name, method) ->
                listOf(
                    DynamicTest.dynamicTest("when $name, it calls kid elements updateOffset ") {
                        val (panel, kid) = makePanelAndKid()
                        val kidOffsetOld = Vec2(0f, 0f)
                        kid.updateOffset(kidOffsetOld.clone())
                        val panelMovement = Vec2(1f, 0f)
                        method(panel, panelMovement)

                        verify { kid.updateOffset(any()) }
                    },
                    DynamicTest.dynamicTest("when $name, it calculates the drag handle correctly") {
                        val panel = makePanel()

                        assert(panel.handleLeftClickPress(inBounds))

                        val panelMovement = Vec2(0f, 100f)
                        method(panel, panelMovement)

                        assert(!panel.handleLeftClickPress(inBounds))
                    }
                )
            }
    }

    @Test
    fun `when handleHover in bounds, it calls kid elements handleHover`() {
        val (panel, kid) = makePanelAndKid()
        panel.handleHover(inBounds)

        verify { kid.handleHover(inBounds) }
    }

    @Test
    fun `when handleHover out of bounds, it also calls kid elements handleHover`() {
        val (panel, kid) = makePanelAndKid()
        panel.handleHover(outOfBounds)

        verify { kid.handleHover(outOfBounds) }
    }

    @DisplayName("handleLeftClickPress(location)")
    @Nested
    inner class HandleLeftClickPress {

        @Test
        fun `when location out of bounds, it does nothing`() {
            val (panel, kid) = makePanelAndKid()
            panel.handleLeftClickPress(outOfBounds)

            verify(inverse = true) { kid.handleLeftClickPress(outOfBounds) }
        }

        @Test
        fun `when location in bounds, it calls kid elements handleLeftClickPress`() {
            val (panel, kid) = makePanelAndKid()
            assert(panel.handleLeftClickPress(inBounds))

            verify { kid.handleLeftClickPress(inBounds) }
        }

        @Test
        fun `when location in bounds, it sets panel isPressed`() {
            val panel = makePanel()

            assert(panel.handleLeftClickPress(inBounds))
        }
    }

    @DisplayName("handleLeftClickRelease(location)")
    @Nested
    inner class HandleLeftClickRelease {

        @Test
        fun `when location out of bounds, it calls kid elements handleLeftClickPress`() {
            val (panel, kid) = makePanelAndKid()
            every { kid.handleLeftClickRelease(any()) } returns true

            assert(panel.handleLeftClickRelease(outOfBounds))

            verify { kid.handleLeftClickRelease(outOfBounds) }
        }

        @Test
        fun `when no kid elements, it returns false`() {
            val panel = makePanel()

            assert(!panel.handleLeftClickRelease(outOfBounds))
        }
    }

    @DisplayName("handleLeftClickDrag(location, movement)")
    @Nested
    inner class HandleLeftClickDrag {

        @Test
        fun `when has kid elements, it calls kid elements handleLeftClickDrag`() {
            val (panel, kid) = makePanelAndScrollKid()
            panel.handleLeftClickDrag(outOfBounds, movement)

            verify { kid.handleLeftClickDrag(outOfBounds, movement) }
        }

        @Test
        fun `when kid element accepts handleLeftClickDrag, it does not call didParentDrag`() {
            val (panel, kid) = makePanelAndScrollKid(true)
            every { kid.handleLeftClickDrag(any(), any()) } returns true

            val panelOffsetOld = panel.offset.clone()
            assert(panel.handleLeftClickDrag(outOfBounds, movement))
            assert(panelOffsetOld == panel.offset)

            verify { kid.handleLeftClickDrag(outOfBounds, movement) }
        }

        @Test
        fun `when kid element rejects handleLeftClickDrag, it calls didParentDrag`() {
            val (panel, kid) = makePanelAndKid(true)
            every { kid.handleLeftClickDrag(any(), any()) } returns false

            val panelOffsetOld = panel.offset.clone()
            panel.handleLeftClickPress(inBounds)
            assert(panel.handleLeftClickDrag(inBounds, movement))
            assert(panelOffsetOld != panel.offset)
        }

        @Test
        fun `when no kid elements, it returns false`() {
            val panel = makePanel()

            assert(!panel.handleLeftClickDrag(inBounds, movement))
        }
    }

    @DisplayName("handleScroll(location, movement)")
    @Nested
    inner class HandleScroll {

        @Test
        fun `when location out of bounds, it does nothing`() {
            val (panel, kid) = makePanelAndScrollKid()
            panel.handleScroll(outOfBounds, movement)

            verify(inverse = true) { kid.handleScroll(outOfBounds, movement) }
        }

        @Test
        fun `when has kid elements, it calls kid elements handleScroll`() {
            val (panel, kid) = makePanelAndScrollKid()
            panel.handleScroll(inBounds, movement)

            verify { kid.handleScroll(inBounds, movement) }
        }
    }

    @Test
    fun `when addKids, it updates kid elements offsets`() {
        val kid: GuiElement = mockk(relaxed = true)

        val panelOffsetNew = Vec2(10f, 10f)
        val panel = makePanel().also { it.updateOffset(panelOffsetNew) }
        val kidOffsetOld = Vec2(-5f, 5f)
        every { kid.offset.clone() } returns kidOffsetOld
        panel.addKids(listOf(kid))

        verify { kid.updateOffset(kidOffsetOld.add(panelOffsetNew)) }
    }

    @Test
    fun `when addKid, it updates kid element offset`() {
        val kid: GuiElement = mockk(relaxed = true)

        val panelOffsetNew = Vec2(10f, 10f)
        val panel = makePanel().also { it.updateOffset(panelOffsetNew) }
        val kidOffsetOld = Vec2(-5f, 5f)
        every { kid.offset.clone() } returns kidOffsetOld
        panel.addKid(kid)

        verify { kid.updateOffset(kidOffsetOld.add(panelOffsetNew)) }
    }

    private fun makePanel(draggable: Boolean = false): GuiPanel =
        GuiPanel(baseOffset(), baseScale(), draggable = draggable)

    private fun makePanelAndKid(draggable: Boolean = false): Pair<GuiPanel, HasDrag> {
        val kid: HasDrag = mockk(relaxed = true)
        val panel = makePanel(draggable).also { it.addKid(kid) }

        return Pair(panel, kid)
    }

    private fun makePanelAndScrollKid(draggable: Boolean = false): Pair<GuiPanel, HasScroll> {
        val kid: HasScroll = mockk(relaxed = true)
        val panel = makePanel(draggable).also { it.addKid(kid) }

        return Pair(panel, kid)
    }
}
