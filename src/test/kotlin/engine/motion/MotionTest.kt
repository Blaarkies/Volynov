package engine.motion

import engine.freeBody.FreeBody
import engine.gameState.GameState
import io.mockk.every
import io.mockk.mockk
import org.jbox2d.common.Vec2
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import utility.Common.makeVec2

internal class MotionTest {

    @Test
    fun `addNewTrailer when called with values, adds new trailers at correct locations`() {
        val subject = Motion()
        val freeBody: FreeBody = mockk()

        every { freeBody.worldBody.linearVelocity } returns makeVec2(10)

        every { freeBody.worldBody.position } returns Vec2(10f, 10f)
        subject.addNewTrailer(freeBody)

        every { freeBody.worldBody.position } returns Vec2(20f, 10f)
        subject.addNewTrailer(freeBody)

        val lastTrailer = subject.trailers.chunked(2).last()
        assertEquals(20f, lastTrailer[0])
        assertEquals(10f, lastTrailer[1])
    }

}
