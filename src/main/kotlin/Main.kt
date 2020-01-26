import app.IGameLogic
import kotlin.system.exitProcess

fun main() {
    try {
        val gameLogic: IGameLogic = AppLogic()
        val gameEngine = AppRunner("Volynov", 1000, 1000, true, gameLogic)
        gameEngine.run()
    } catch (exception: Exception) {
        exception.printStackTrace()
        exitProcess(-1)
    }
}
