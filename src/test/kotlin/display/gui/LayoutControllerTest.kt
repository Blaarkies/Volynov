package display.gui

import dI
import display.draw.Drawer
import display.gui.LayoutController.getOffsetForLayoutPosition
import display.gui.element.GuiButton
import io.mockk.mockk
import org.jbox2d.common.Vec2
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertDoesNotThrow
import utility.Common.makeVec2
import utility.toSign

internal class LayoutControllerTest {

    init {
        val drawer: Drawer = mockk(relaxed = true)
        dI.drawer = drawer
    }
    val baseOffset = { Vec2() }
    val baseScale = { makeVec2(1) }

    private fun getDirectionName(direction: Boolean): String = if (direction) "horizontal" else "vertical"

    @TestFactory
    fun `setElementsInStacks`(): List<DynamicTest> {
        return listOf(
            DynamicTest.dynamicTest("when horizontal empty list") { LayoutController.setElementsInColumns(listOf()) },
            DynamicTest.dynamicTest("when vertical empty list") { LayoutController.setElementsInRows(listOf()) }
        ) + listOf(
            SetElementsInStacksTest("list of 1, it does nothing to the guiElement",
                null, 0f, false, 0f),
            SetElementsInStacksTest("list of 2, it moves elements correctly",
                null, 0f, false, 0f, 2f),
            SetElementsInStacksTest("list of 3, it moves elements correctly",
                null, 0f, false, 0f, 2f, 4f),
            SetElementsInStacksTest("list of 2 and GAP, it moves elements correctly",
                null, 10f, false, 0f, 12f),
            SetElementsInStacksTest("list of 2 and CENTERING, it moves elements correctly",
                null, 0f, true, -1f, 1f),
            SetElementsInStacksTest("list of 2 and GAP AND CENTERING, it moves elements correctly",
                null, 10f, true, -6f, 6f),
            SetElementsInStacksTest("list of 3 and DISTINCT sizes, it moves elements correctly", listOf(
                ElementConfig(scale = makeVec2(1)),
                ElementConfig(scale = makeVec2(3)),
                ElementConfig(scale = makeVec2(2))
            ), 0f, false, 0f, 4f, 9f)
        )
            .flatMap { config -> listOf(true, false).map { Pair(config, it) } }
            .map { (config, isHorizontal) ->
                DynamicTest.dynamicTest("when ${getDirectionName(isHorizontal)} ${config.name}") {
                    checkListOfN(config.elementConfigs, isHorizontal, config.gap, config.centered,
                        config.values.map { it * isHorizontal.toSign() }.toList())
                }
            }
    }

    private fun checkListOfN(configList: List<ElementConfig>?,
                             isHorizontal: Boolean,
                             gap: Float,
                             centered: Boolean,
                             expectedValues: List<Float>) {
        val elements = expectedValues
            .zip(configList
                ?: (0..expectedValues.size).map { ElementConfig() })
            .map { (expectedValue, config) ->
                val element = GuiButton(config.offset, config.scale)
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
            assert(getMeasure(element.offset) == expectedValue) {
                "When stacking ${getDirectionName(isHorizontal)}, GuiElement at ${getMeasure(element.offset)} " +
                        "expected to be $expectedValue"
            }
        }
    }

    inner class ElementConfig(val offset: Vec2 = baseOffset(),
                              val scale: Vec2 = baseScale())

    class SetElementsInStacksTest(val name: String,
                                  val elementConfigs: List<ElementConfig>? = listOf(),
                                  val gap: Float,
                                  val centered: Boolean,
                                  vararg expectedValues: Float) {

        val values = expectedValues
    }

    @TestFactory
    fun `getOffsetForLayoutPosition when kid is smaller than parent`(): List<DynamicTest> {
        return listOf(
            Pair(LayoutPosition.TOP_LEFT, Vec2(-1f, 1f)),
            Pair(LayoutPosition.TOP_RIGHT, Vec2(1f, 1f)),
            Pair(LayoutPosition.BOTTOM_LEFT, Vec2(-1f, -1f)),
            Pair(LayoutPosition.BOTTOM_RIGHT, Vec2(1f, -1f))
        ).let { makeLayoutPositionTest(it, 2, 1) }
    }

    @TestFactory
    fun `getOffsetForLayoutPosition when kid is larger than parent`(): List<DynamicTest> {
        return listOf(
            Pair(LayoutPosition.TOP_LEFT, Vec2(1f, -1f)),
            Pair(LayoutPosition.TOP_RIGHT, Vec2(-1f, -1f)),
            Pair(LayoutPosition.BOTTOM_LEFT, Vec2(1f, 1f)),
            Pair(LayoutPosition.BOTTOM_RIGHT, Vec2(-1f, 1f))
        ).let { makeLayoutPositionTest(it, 1, 2) }
    }

    private fun makeLayoutPositionTest(testCases: List<Pair<LayoutPosition, Vec2>>,
                                       parentRadius: Int,
                                       kidRadius: Int): List<DynamicTest> {
        return testCases.map { (enum, expected) ->
            DynamicTest.dynamicTest("${enum.name} corner placement") {
                val parentScale = makeVec2(parentRadius)
                val kidScale = makeVec2(kidRadius)
                val kidOffset = getOffsetForLayoutPosition(enum, parentScale, kidScale)
                assert(kidOffset == expected) { "Offset $kidOffset was expected to be $expected" }
            }
        }
    }

    @Test
    fun `getOffsetForLayoutPosition when parameters are bad`() {
        assertDoesNotThrow {
            val kidOffset = getOffsetForLayoutPosition(LayoutPosition.TOP_LEFT, Vec2(), Vec2())
            assert(kidOffset.length() == 0f) { "Offset $kidOffset was expected to be (0,0)" }
        }
    }

}
