package engine.motion

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class LocationTest {

    @Test
    fun addLocation() {
        val subject = Location()
        subject.addLocation(1.0, 2.0, 3.0)
        assertEquals(1.0, subject.x)
        assertEquals(2.0, subject.y)
        assertEquals(3.0, subject.h)
    }
}
