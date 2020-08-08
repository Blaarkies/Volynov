package utility

class StopWatch {

    val currentTime
        get() = System.currentTimeMillis()

    val elapsedTime
        get() = (currentTime - lastTimestamp)

    private var lastTimestamp = currentTime

    fun reset() {
        lastTimestamp = currentTime
    }

}
