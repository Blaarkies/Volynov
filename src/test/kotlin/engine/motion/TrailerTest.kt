package engine.motion

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class TrailerTest {

    @Test
    fun trailerLocationIndependent() {
        val location = Location(.0, .0)
        val trailer = Trailer(location)
        assertEquals(.0, trailer.location.x)
        assertEquals(.0, trailer.location.y)

        location.addLocation(1.0, 1.0)
        assertNotEquals(1.0, trailer.location.x)
        assertNotEquals(1.0, trailer.location.y)
    }
}
