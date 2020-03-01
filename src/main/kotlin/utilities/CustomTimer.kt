package utilities

class CustomTimer {

    var lastLoopTime = 0f
        private set

    fun init() {
        lastLoopTime = time
    }

    val time: Float
        get() = System.nanoTime() / 1_000_000_000f

    val elapsedTime: Float
        get() {
            val time = time
            val elapsedTime = (time - lastLoopTime)
            lastLoopTime = time
            return elapsedTime
        }

}
