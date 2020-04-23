package app

import display.Window
import utility.CustomTimer

class AppRunner(
    windowTitle: String,
    width: Int,
    height: Int,
    vSync: Boolean,
    private val gameLogic: IGameLogic,
    private val customTimer: CustomTimer = CustomTimer(),
    private val window: Window = Window(
        windowTitle,
        width,
        height,
        vSync
    ),
    private val debugMode: Boolean
) : Runnable {

    private val targetFps = 60
    private val targetUps = 60
    private val interval = 1f / targetUps

    override fun run() {
        try {
            init(debugMode = debugMode)
            gameLoop()
        } catch (exception: Exception) {
            exception.printStackTrace()
        } finally {
            gameLogic.cleanup()
        }
    }

    @Throws(Exception::class)
    private fun init(debugMode: Boolean) {
        window.init(debugMode = debugMode)
        customTimer.init()
        gameLogic.init(window)
    }

    private fun gameLoop() {
        var elapsedTime: Float
        var accumulator = 0f
        val running = true
        while (running && !window.windowShouldClose()) {
            elapsedTime = customTimer.elapsedTime
            accumulator += elapsedTime

            while (accumulator >= interval) {
                gameLogic.update(interval)
                accumulator -= interval
            }

            gameLogic.render(window)
            window.update()

            if (!window.isVSync()) {
                sync()
            }
        }
    }

    private fun sync() {
        val loopSlot = 1f / targetFps
        val endTime = customTimer.lastLoopTime + loopSlot
        while (customTimer.time < endTime) {
            try {
                Thread.sleep(1)
            } catch (exception: InterruptedException) {
            }
        }
    }

}
