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
        val screenX = if (isDebugMode) 1000 else 1920
        val screenY = if (isDebugMode) 1000 else 1080
        dI.isDebugMode = isDebugMode

        val gameLogic: IGameLogic = AppLogic()
        val gameEngine = AppRunner("Volynov", screenX, screenY, true, gameLogic)
        gameEngine.run()
    } catch (exception: Exception) {
        exception.printStackTrace()
        exitProcess(-1)
    }
}

val dI = DependencyInjectionContainer()
