import app.IGameLogic
import kotlin.system.exitProcess

fun main() {
    try {
        val gameLogic: IGameLogic = AppLogic()
        val gameEngine = AppRunner("GAME", 960, 960, vSync = true, gameLogic = gameLogic)
        gameEngine.run()
    } catch (exception: Exception) {
        exception.printStackTrace()
        exitProcess(-1)
    }
}

/*
fun oldmain() {
    val fps = 0.1
    val msPerFrame = 1000L / fps
    val gameState = GameState()
  val display = Animator(gameState, 800, 600)
    val renderer: Renderer = Renderer()
    val window: Window = Window("Volynov", 600, 480, true)
    gameState.addPlayer(100.0, 100.0, 0.0, 0.8, 0.3, 0.1, "1")
    gameState.addPlanet(300.0, 300.0, 0.0, 0.0, -0.7, -0.01, "A", 325.0, 30.0, 1000.0)
    Timer("physicsClock", false).schedule(0, (msPerFrame/2).toLong()) {
        gameState.tickClock()
    }

    Timer("displayClock", false).schedule(0, msPerFrame.toLong()) {
    }
}
*/

