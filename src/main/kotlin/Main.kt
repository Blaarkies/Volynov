import app.AppLogic
import app.AppRunner
import app.IGameLogic
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.system.exitProcess

fun main() = runBlocking {
    try {
        val configLine = File("config.txt").let {
            if (it.exists()) it.readLines()[0] else "isDebugMode=1"
        }
        val isDebugMode = (configLine == "isDebugMode=1")
        val screenX = if (isDebugMode) 800 else 1920
        val screenY = if (isDebugMode) 800 else 1080

        val gameLogic: IGameLogic = AppLogic()
        val gameEngine = AppRunner("Volynov", screenX, screenY, true, gameLogic, debugMode = isDebugMode)
        gameEngine.run()
    } catch (exception: Exception) {
        exception.printStackTrace()
        exitProcess(-1)
    }
}
