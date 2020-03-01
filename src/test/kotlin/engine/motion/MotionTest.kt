package engine.motion

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.util.*

internal class MotionTest {

    @Test
    fun create_new_trailers() {
        val subject = Motion()
        subject.addNewTrailer(10f, 10f)
        subject.addNewTrailer(20f, 10f)

        val lastTrailer = subject.trailers.chunked(2).last()
        assertEquals(20f, lastTrailer[0])
        assertEquals(10f, lastTrailer[1])
    }

}
