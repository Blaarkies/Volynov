package engine.motion

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import utilities.Utils
import kotlin.math.PI

internal class DirectorTest {

    @Test
    fun distance() {
        val rightPointed = Director.getDistance(0f, 0f, 1f, 1f)
        assertEquals(1.414f, Utils.roundFloat(rightPointed, 3))

        val leftPointed = Director.getDistance(0f, 0f, -1f, -1f)
        assertEquals(1.414f, Utils.roundFloat(leftPointed, 3))
    }

    @Test
    fun direction() {
        // Test direction from client, to server
        val pointed0 = Director.getDirection(0f, 0f, -1f, 0f)
        assertEquals(0f, pointed0, "0")

        val pointed45 = Director.getDirection(0f, 0f, -1f, -1f)
        assertEquals(PI / 4, pointed45, "45")

        val pointed90 = Director.getDirection(0f, 0f, 0f, -1f)
        assertEquals(PI / 2, pointed90, "90")

        val pointed135 = Director.getDirection(0f, 0f, 1f, -1f)
        assertEquals(PI * 3 / 4, pointed135, "135")

        val pointed180 = Director.getDirection(0f, 0f, 1f, 0f)
        assertEquals(PI, pointed180, "180")

        val pointed225 = Director.getDirection(0f, 0f, 1f, 1f)
        assertEquals(-PI * 3 / 4, pointed225, "225")

        val pointed270 = Director.getDirection(0f, 0f, 0f, 1f)
        assertEquals(-PI / 2, pointed270, "270")

        val pointed315 = Director.getDirection(0f, 0f, -1f, 1f)
        assertEquals(-PI / 4, pointed315, "315")

        val vector = Director.getDirection(1f, 0f)
        assertEquals(0f, vector, "vector")
    }

}
