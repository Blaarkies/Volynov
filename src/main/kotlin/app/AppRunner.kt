import utils.CustomTimer
import app.IGameLogic
import display.Window

class AppRunner(
    windowTitle: String,
    width: Int,
    height: Int,
    vSync: Boolean,
    private val gameLogic: IGameLogic,
    private val customTimer: CustomTimer = CustomTimer(),
    private val window: Window = Window(windowTitle, width, height, vSync)
) : Runnable {

    private val targetFps = 60
    private val targetUps = 120
    private val interval = 1f / targetUps

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
            gameLogic.input(window)
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
