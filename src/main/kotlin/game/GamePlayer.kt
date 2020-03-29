package game

import engine.freeBody.Vehicle
import engine.freeBody.Warhead

class GamePlayer(
    val name: String,
    val type: GamePlayerTypes = GamePlayerTypes.HUMAN,
    var vehicle: Vehicle? = null,
    val playerAim: PlayerAim = PlayerAim()
) {

    val warheads = mutableListOf<Warhead>()

}
