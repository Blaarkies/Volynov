package app

import dI
import display.Window
import utility.CustomTimer

class AppRunner(
    windowTitle: String,
    width: Int,
    height: Int,
    vSync: Boolean,
    private val gameLogic: IGameLogic,
    private val customTimer: CustomTimer = CustomTimer()
) : Runnable {

    private val targetFps = 60
    private val targetUps = 60
    private val interval = 1f / targetUps

    private val window = Window(windowTitle, width, height, vSync)

    init {
        dI.window = window
        dI.init()
    }

    override fun run() {
        try {
            init()
            gameLoop()
        } catch (exception: Exception) {
            exception.printStackTrace()
        } finally {
            gameLogic.cleanup()
        }
    }

    @Throws(Exception::class)
    private fun init() {
        window.init()
        customTimer.init()
        gameLogic.init()
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

            gameLogic.render()
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
