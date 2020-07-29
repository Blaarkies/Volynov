package display.graphic

import display.gui.base.GuiElement
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.jbox2d.common.Vec2
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import utility.Common
import utility.Common.makeVec2

internal class SnipRegionTest {

    @DisplayName("intersect(snipRegion)")
    @Nested
    inner class Intersect {

        @Test
        fun `when B is null, returns A`() {
            val a = SnipRegion(Vec2(-2f, -2f), Vec2(2f, 2f))

            val c = a.intersect(null)!!

            assert(c.x == a.x)
            assert(c.y == a.y)
            assert(c.sizeX == a.sizeX)
            assert(c.sizeY == a.sizeY)
        }

        @Test
        fun `when B is outside A, returns 0`() {
            val a = SnipRegion(Vec2(-2f, -2f), Vec2(2f, 2f))
            val b = SnipRegion(Vec2(-4f, -4f), Vec2(-2f, -2f))

            val c = a.intersect(b)!!

            assert(c.x == 0)
            assert(c.y == 0)
            assert(c.sizeX == 0)
            assert(c.sizeY == 0)
        }

        @Test
        fun `when B is inside A, returns B`() {
            val a = SnipRegion(Vec2(-2f, -2f), Vec2(2f, 2f))
            val b = SnipRegion(Vec2(-1f, -1f), Vec2(1f, 1f))

            val c = a.intersect(b)!!

            assert(c.x == b.x)
            assert(c.y == b.y)
            assert(c.sizeX == b.sizeX)
            assert(c.sizeY == b.sizeY)
        }

        @Test
        fun `when B is overlapping A bottom left, returns intersection`() {
            val a = SnipRegion(Vec2(-2f, -2f), Vec2())
            val b = SnipRegion(Vec2(-3f, -3f), Vec2(-1f, -1f))

            val c = a.intersect(b)!!

            assert(c.x == -2)
            assert(c.y == -2)
            assert(c.sizeX == 1)
            assert(c.sizeY == 1)
        }

        @Test
        fun `when B is overlapping A top left, returns intersection`() {
            val a = SnipRegion(Vec2(-2f, -2f), Vec2())
            val b = SnipRegion(Vec2(-3f, -1f), Vec2(-1f, 1f))

            val c = a.intersect(b)!!

            assert(c.x == -2)
            assert(c.y == -1)
            assert(c.sizeX == 1)
            assert(c.sizeY == 1)
        }

        @Test
        fun `when B is overlapping A top right, returns intersection`() {
            val a = SnipRegion(Vec2(-2f, -2f), Vec2())
            val b = SnipRegion(Vec2(-1f, -1f), Vec2(1f, 1f))

            val c = a.intersect(b)!!

            assert(c.x == -1)
            assert(c.y == -1)
            assert(c.sizeX == 1)
            assert(c.sizeY == 1)
        }

        @Test
        fun `when B is overlapping A bottom right, returns intersection`() {
            val a = SnipRegion(Vec2(-2f, -2f), Vec2())
            val b = SnipRegion(Vec2(-1f, -3f), Vec2(1f, -1f))

            val c = a.intersect(b)!!

            assert(c.x == -1)
            assert(c.y == -2)
            assert(c.sizeX == 1)
            assert(c.sizeY == 1)
        }

    }

    @DisplayName("companion create(guiElement)")
    @Nested
    inner class Create {

        @Test
        fun `when element is centered, snip region is correct`() {
            val element: GuiElement = mockk()
            every { element.offset } returns Vec2()
            every { element.scale } returns makeVec2(1)

            val snipRegion = SnipRegion.create(element)

            assert(snipRegion.bottomLeft.sub(Vec2(-2f, -2f)).length() == 0f)
            assert(snipRegion.topRight.sub(Vec2(2f, 2f)).length() == 0f)
        }

        @Test
        fun `when element is bottom left, snip region is correct`() {
            val element: GuiElement = mockk()
            every { element.offset } returns Vec2(-1f, -1f)
            every { element.scale } returns makeVec2(1)

            val snipRegion = SnipRegion.create(element)

            assert(snipRegion.bottomLeft.sub(Vec2(-3f, -3f)).length() == 0f)
            assert(snipRegion.topRight.sub(Vec2(1f, 1f)).length() == 0f)
        }

        @Test
        fun `when element is top left, snip region is correct`() {
            val element: GuiElement = mockk()
            every { element.offset } returns Vec2(-1f, 1f)
            every { element.scale } returns makeVec2(1)

            val snipRegion = SnipRegion.create(element)

            assert(snipRegion.bottomLeft.sub(Vec2(-3f, -1f)).length() == 0f)
            assert(snipRegion.topRight.sub(Vec2(1f, 3f)).length() == 0f)
        }

        @Test
        fun `when element is top right, snip region is correct`() {
            val element: GuiElement = mockk()
            every { element.offset } returns Vec2(1f, 1f)
            every { element.scale } returns makeVec2(1)

            val snipRegion = SnipRegion.create(element)

            assert(snipRegion.bottomLeft.sub(Vec2(-1f, -1f)).length() == 0f)
            assert(snipRegion.topRight.sub(Vec2(3f, 3f)).length() == 0f)
        }

        @Test
        fun `when element is bottom right, snip region is correct`() {
            val element: GuiElement = mockk()
            every { element.offset } returns Vec2(1f, -1f)
            every { element.scale } returns makeVec2(1)

            val snipRegion = SnipRegion.create(element)

            assert(snipRegion.bottomLeft.sub(Vec2(-1f, -3f)).length() == 0f)
            assert(snipRegion.topRight.sub(Vec2(3f, 1f)).length() == 0f)
        }

    }

}
