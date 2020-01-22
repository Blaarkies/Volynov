import app.IGameLogic
import kotlin.system.exitProcess

fun main() {
    try {
        val gameLogic: IGameLogic = AppLogic()
        val gameEngine = AppRunner("Volynov", 960, 960, vSync = true, gameLogic = gameLogic)
        gameEngine.run()
    } catch (exception: Exception) {
        exception.printStackTrace()
        exitProcess(-1)
    }
}
