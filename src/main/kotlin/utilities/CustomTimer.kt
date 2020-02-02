package utilities

class CustomTimer {

    var lastLoopTime = 0.0
        private set

    fun init() {
        lastLoopTime = time
    }

    val time: Double
        get() = System.nanoTime() / 1000000000.0

    val elapsedTime: Float
        get() {
            val time = time
            val elapsedTime = (time - lastLoopTime).toFloat()
            lastLoopTime = time
            return elapsedTime
        }

}
