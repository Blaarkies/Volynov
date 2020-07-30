package engine.motion

import engine.freeBody.FreeBody
import utility.toList

class Motion(val trailers: MutableList<Float> = mutableListOf(),
             private var trailerQuantity: Int = 40) {

    private var lastTrailer: Array<Float>? = null

    fun addNewTrailer(freeBody: FreeBody) {
        val (x, y) = freeBody.worldBody.position.toList()

        if (lastTrailer == null) {
            lastTrailer = arrayOf(x, y)
            return
        }
        val distance = Director.getDistance(x, y, lastTrailer!![0], lastTrailer!![1])
        val maxDistance = freeBody.worldBody.linearVelocity.length() * .06f
        if (distance > maxDistance) {
            val nowTrailer = arrayOf(x, y)
            lastTrailer = nowTrailer
            trailers.addAll(nowTrailer)

            if (trailers.size > trailerQuantity) {
                trailers.removeAt(0)
                trailers.removeAt(0)
            }
        }
    }

    companion object {

        fun addNewTrailers(freeBodies: List<FreeBody>) {
            freeBodies.forEach { it.motion.addNewTrailer(it) }
        }

    }
}
