package display.gui

import display.draw.Drawer
import io.mockk.mockk
import org.jbox2d.common.Vec2
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import utility.Common.makeVec2

internal class LayoutControllerTest {

    val mockDrawer: Drawer = mockk()
    val baseOffset = { Vec2() }
    val baseScale = { makeVec2(1) }

    @Test
    fun `setElementsInColumns when empty list, it does nothing`() {
        assertDoesNotThrow {
            LayoutController.setElementsInColumns(listOf())
        }
    }

    @Test
    fun `setElementsInColumns when list of 1, it does nothing to the guiElement`() {
        checkListOfN(null, true, 0f, false, 0f)
    }

    @Test
    fun `setElementsInColumns when list of 2, it moves elements correctly`() {
        checkListOfN(null, true, 0f, false, 0f, 2f)
    }

    @Test
    fun `setElementsInColumns when list of 3, it moves elements correctly`() {
        checkListOfN(null, true, 0f, false, 0f, 2f, 4f)
    }

    @Test
    fun `setElementsInColumns when list of 2 and gap, it moves elements correctly`() {
        checkListOfN(null, true, 10f, false, 0f, 12f)
    }

    @Test
    fun `setElementsInColumns when list of 2 and centering, it moves elements correctly`() {
        checkListOfN(null, true, 0f, true, -1f, 1f)
    }

    @Test
    fun `setElementsInColumns when list of 2 and gap and centering, it moves elements correctly`() {
        checkListOfN(null, true, 10f, true, -6f, 6f)
    }

    @Test
    fun `setElementsInColumns when list of 3 and distinct sizes, it moves elements correctly`() {
        checkListOfN(listOf(
            ElementConfig(scale = makeVec2(1)),
            ElementConfig(scale = makeVec2(3)),
            ElementConfig(scale = makeVec2(2))
        ), true, 0f, false, 0f, 4f, 9f)
    }

    @Test
    fun `setElementsInRows when empty list, it does nothing`() {
        assertDoesNotThrow {
            LayoutController.setElementsInRows(listOf())
        }
    }

    @Test
    fun `setElementsInRows when list of 1, it does nothing to the guiElement`() {
        checkListOfN(null, false, 0f, false, 0f)
    }

    @Test
    fun `setElementsInRows when list of 2, it moves elements correctly`() {
        checkListOfN(null, false, 0f, false, 0f, -2f)
    }

    @Test
    fun `setElementsInRows when list of 3, it moves elements correctly`() {
        checkListOfN(null, false, 0f, false, 0f, -2f, -4f)
    }

    @Test
    fun `setElementsInRows when list of 2 and gap, it moves elements correctly`() {
        checkListOfN(null, false, 10f, false, 0f, -12f)
    }

    @Test
    fun `setElementsInRows when list of 2 and centering, it moves elements correctly`() {
        checkListOfN(null, false, 0f, true, 1f, -1f)
    }

    @Test
    fun `setElementsInRows when list of 2 and gap and centering, it moves elements correctly`() {
        checkListOfN(null, false, 10f, true, 6f, -6f)
    }

    @Test
    fun `setElementsInRows when list of 3 and distinct sizes, it moves elements correctly`() {
        checkListOfN(listOf(
            ElementConfig(scale = makeVec2(1)),
            ElementConfig(scale = makeVec2(3)),
            ElementConfig(scale = makeVec2(2))
        ), false, 0f, false, 0f, -4f, -9f)
    }

    private fun checkListOfN(configList: List<ElementConfig>?,
                             isHorizontal: Boolean,
                             gap: Float,
                             centered: Boolean,
                             vararg expectedValues: Float) {
        val elements = expectedValues
            .zip(configList
                ?: (0..expectedValues.size).map { ElementConfig() })
            .map { (expectedValue, config) ->
                val element = GuiElement(mockDrawer, config.offset, config.scale)
                Pair(element, expectedValue)
            }

        if (isHorizontal) {
            LayoutController.setElementsInColumns(
                elements.map { (element, _) -> element }, gap, centered)
        } else {
            LayoutController.setElementsInRows(
                elements.map { (element, _) -> element }, gap, centered)
        }
        val getMeasure = if (isHorizontal) { vec: Vec2 -> vec.x }
        else { vec: Vec2 -> vec.y }

        elements.forEach { (element, expectedValue) ->
            assert(getMeasure(element.offset) == expectedValue)
            { "GuiElement at ${getMeasure(element.offset)} expected to be $expectedValue" }
        }
    }

    inner class ElementConfig(val offset: Vec2 = baseOffset(),
                              val scale: Vec2 = baseScale())

}
