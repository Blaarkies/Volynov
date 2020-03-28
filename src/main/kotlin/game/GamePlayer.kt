package game

import engine.freeBody.Vehicle

class GamePlayer(
    val name: String,
    val type: GamePlayerTypes = GamePlayerTypes.HUMAN,
    var vehicle: Vehicle? = null
)
