import app.AppLogic
import app.IGameLogic
import kotlinx.coroutines.*
import kotlin.system.exitProcess

fun main() = runBlocking {
    try {
        val gameLogic: IGameLogic = AppLogic()
        val gameEngine = AppRunner("Volynov", 1000, 1000, true, gameLogic)
        gameEngine.run()
    } catch (exception: Exception) {
        exception.printStackTrace()
        exitProcess(-1)
    }
}


//fun main() =
//
//    runBlocking {
//        val startCoR = System.nanoTime()
//        val jobs = List(50) {
//            launch(Dispatchers.Default) {
//                repeat(200_000_000) {
//                    ln(it.toDouble())
//                }
//            }
//        }
//        jobs.forEach { it.join() }
//
//        val endCoR = System.nanoTime()
//        println("CoRoutines= ${(endCoR - startCoR) / 1_000_000_000f} █")
//
//
//        val start = System.nanoTime()
//        repeat(200_000_000) {
//            ln(it.toDouble())
//        }
//        val end = System.nanoTime()
//        println("Seq= ${(end - start) / 1_000_000_000f} █")
//        println("Seq*50= ${(end - start) * 50 / 1_000_000_000f} █")
//
//    }
