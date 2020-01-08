package engine.motion

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ForceTest {

    @Test
    fun addForce() {
        val subject = Force(.0, .0)
        subject.add(Force(1.0, 2.0))
        assertEquals(1.0, subject.x)
        assertEquals(2.0, subject.y)
    }
}
