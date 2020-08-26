package game

import dI
import display.events.MouseButtonEvent
import engine.freeBody.Vehicle
import engine.freeBody.Warhead
import game.fuel.Fuel
import game.fuel.FuelType
import io.reactivex.Observable
import utility.Common.getTimingFunctionEaseIn
import kotlin.math.floor

class GamePlayer(
    var name: String,
    val type: GamePlayerType = GamePlayerType.HUMAN,
    var vehicle: Vehicle? = null,
    val playerAim: PlayerAim = PlayerAim(),
    var score: Float = 0f,
    var cash: Float = 1000f
) {

    val warheads = mutableListOf<Warhead>()
    val purchaseHistory = mutableListOf<String>()

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

    fun clone(): GamePlayer = GamePlayer(name, GamePlayerType.CLONE, null, playerAim.clone(), 0f, 0f)

    fun startJump() {
        val selectedFuel = playerAim.selectedFuelDescriptor
        buyItem(selectedFuel.name, selectedFuel.price, dI.gameState.tickTime)

        vehicle?.startJump(playerAim)
        playerAim.selectedFuel = null
    }

    fun thrustVehicle(event: Observable<MouseButtonEvent>) {
        vehicle?.thrustVehicle(event)
    }

    fun addShield() {
        val selectedShield = playerAim.selectedShieldDescriptor
        if (selectedShield != null) {
            buyItem(selectedShield.name, selectedShield.price, dI.gameState.tickTime)
        }

        vehicle?.addShield(playerAim)
        playerAim.selectedShield = null
    }

    fun buyItem(name: String, price: Int, gameTime: Float) {
        val floatToPadded = { float: Float -> float.toInt().toString().padStart(2, '0') }
        val minutes = gameTime / (1000 * 60)
        val seconds = minutes.rem(1) * 60
        val readableTime = "${floatToPadded(floor(minutes))}:${floatToPadded(seconds)}"

        purchaseHistory.add("[$readableTime] item[$name] price[$price] balance[$cash]")
        cash -= price

    }

}
