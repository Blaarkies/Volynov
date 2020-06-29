package game

import engine.freeBody.Vehicle
import engine.freeBody.Warhead
import org.jbox2d.common.Vec2
import utility.Common.getTimingFunctionEaseIn

class GamePlayer(
    var name: String,
    val type: GamePlayerTypes = GamePlayerTypes.HUMAN,
    var vehicle: Vehicle? = null,
    val playerAim: PlayerAim = PlayerAim(),
    var score: Float = 0f,
    var cash: Float = 1000f
) {

    val warheads = mutableListOf<Warhead>()

    private val scoreConstant = 10f
    private val cashConstant = 40f
    private val maxStyleConstant = 7.5f
    private val selfHarmConstant = -.5f

    fun scoreDamage(warhead: Warhead,
                    totalDamage: Float,
                    vehicle: Vehicle,
                    tickTime: Float) {
        val selfHarmMultiplier = when (vehicle) {
            this.vehicle -> selfHarmConstant
            else -> 1f
        }
        val noAgeBonusTimeConstant = 2000f

        val age = warhead.getAgeTime(tickTime)
            .minus(noAgeBonusTimeConstant)
            .coerceAtLeast(0f)
            .div(warhead.selfDestructTime - noAgeBonusTimeConstant)
            .let { getTimingFunctionEaseIn(it) * maxStyleConstant + 1f }

        addScore(selfHarmMultiplier * totalDamage * age)
    }

    fun scoreKill(vehicle: Vehicle) {
        val selfHarmMultiplier = when (vehicle) {
            this.vehicle -> selfHarmConstant
            else -> 1f
        }
        addScore(selfHarmMultiplier * 2f)
    }

    private fun addScore(addition: Float) {
        score += addition * scoreConstant
        cash += (addition * cashConstant).coerceAtLeast(0f)
    }

    fun clone(): GamePlayer = GamePlayer(name, type, null, playerAim.clone(), 0f, 0f)

    fun startJump() {
        vehicle?.startJump(playerAim)
    }

    fun thrustVehicle(location: Vec2) {
        vehicle?.thrustVehicle(location)
    }

}
