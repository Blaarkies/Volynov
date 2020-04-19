import app.AppLogic
import app.IGameLogic
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

fun main() = runBlocking {
    try {
        val gameLogic: IGameLogic = AppLogic()
        val gameEngine = AppRunner("Volynov", 1920, 1080, true, gameLogic)
        gameEngine.run()
    } catch (exception: Exception) {
        exception.printStackTrace()
        exitProcess(-1)
    }
}
