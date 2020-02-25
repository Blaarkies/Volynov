package engine.motion

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import utilities.Utils
import kotlin.math.PI

internal class DirectorTest {

    @Test
    fun distance() {
        val rightPointed = Director.getDistance(.0, .0, 1.0, 1.0)
        assertEquals(1.414, Utils.roundDouble(rightPointed, 3))

        val leftPointed = Director.getDistance(.0, .0, -1.0, -1.0)
        assertEquals(1.414, Utils.roundDouble(leftPointed, 3))
    }

    @Test
    fun magnitude() {
        val magnitude = Director.getMagnitude(1.0, 1.0)
        assertEquals(1.414, Utils.roundDouble(magnitude, 3))
    }

    @Test
    fun direction() {
        // Test direction from client, to server
        val pointed0 = Director.getDirection(.0, .0, -1.0, .0)
        assertEquals(.0, pointed0, "0")

        val pointed45 = Director.getDirection(.0, .0, -1.0, -1.0)
        assertEquals(PI / 4, pointed45, "45")

        val pointed90 = Director.getDirection(.0, .0, 0.0, -1.0)
        assertEquals(PI / 2, pointed90, "90")

        val pointed135 = Director.getDirection(.0, .0, 1.0, -1.0)
        assertEquals(PI * 3 / 4, pointed135, "135")

        val pointed180 = Director.getDirection(.0, .0, 1.0, .0)
        assertEquals(PI, pointed180, "180")

        val pointed225 = Director.getDirection(.0, .0, 1.0, 1.0)
        assertEquals(-PI * 3 / 4, pointed225, "225")

        val pointed270 = Director.getDirection(.0, .0, .0, 1.0)
        assertEquals(-PI / 2, pointed270, "270")

        val pointed315 = Director.getDirection(.0, .0, -1.0, 1.0)
        assertEquals(-PI / 4, pointed315, "315")

        val vector = Director.getDirection(1.0, .0)
        assertEquals(.0, vector, "vector")
    }

}
