package utility

class StopWatch {

    private val currentTime
        get() = System.currentTimeMillis()

    val elapsedTime
        get() = (currentTime - lastPhaseTimestamp)

    private var lastPhaseTimestamp = currentTime

    fun reset() {
        lastPhaseTimestamp = currentTime
    }

}
