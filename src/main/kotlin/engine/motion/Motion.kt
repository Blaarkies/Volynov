package engine.motion

import engine.freeBody.FreeBody

class Motion(val trailers: MutableList<Float> = mutableListOf(),
             private var trailerQuantity: Int = 80) {

    private var lastTrailer: Array<Float>? = null

    fun addNewTrailer(x: Float, y: Float) {
        if (lastTrailer == null) {
            lastTrailer = arrayOf(x, y)
            return
        }
        val distance = Director.getDistance(x, y, lastTrailer!![0], lastTrailer!![1])
        if (distance > 5) {
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
            freeBodies.forEach {
                it.motion.addNewTrailer(it.worldBody.position.x, it.worldBody.position.y)
            }
        }

    }
}
