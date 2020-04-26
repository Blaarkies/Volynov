package game

import engine.freeBody.Vehicle
import engine.freeBody.Warhead
import utility.Common.getTimingFunctionEaseIn

class GamePlayer(
    var name: String,
    val type: GamePlayerTypes = GamePlayerTypes.HUMAN,
    var vehicle: Vehicle? = null,
    val playerAim: PlayerAim = PlayerAim(),
    var score: Float = 0f,
    var cash: Float = 0f
) {

    val warheads = mutableListOf<Warhead>()

    fun scoreDamage(warhead: Warhead, totalDamage: Float, vehicle: Vehicle) {
        val selfHarm = when (vehicle) {
            this.vehicle -> -.5f
            else -> 1f
        }
        val noAgeBonusTime = 2000f
        val age = warhead.ageTime
            .minus(noAgeBonusTime)
            .coerceAtLeast(0f)
            .div(warhead.selfDestructTime - noAgeBonusTime)
            .let { getTimingFunctionEaseIn(it) * 5 + 1f }

        addScore(selfHarm * totalDamage * age)
    }

    fun scoreKill(vehicle: Vehicle) {
        val selfHarm = when (vehicle) {
            this.vehicle -> -.5f
            else -> 1f
        }
        addScore(selfHarm * 2f)
    }

    private fun addScore(addition: Float) {
        score += addition
        cash += addition
    }

}
