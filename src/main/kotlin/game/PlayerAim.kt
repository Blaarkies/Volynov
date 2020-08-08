package game

import display.gui.base.GuiElementPhase
import display.gui.elements.GuiMerchandise
import game.fuel.FuelType
import utility.Common.Pi2
import utility.Common.degreeToRadian
import utility.Common.radianToDegree
import kotlin.math.pow

class PlayerAim(angle: Float = 0f, power: Float = 100f) {

    var angle = angle
        set(value) {
            field = value.rem(360f)
        }

    var power = power
        set(value) {
            field = value.coerceIn(0f, 100f)
        }

    var precision = 1f
        set(value) {
            field = value.coerceIn(0f, 10f)
        }

    var selectedFuel: FuelType? = null

    fun getDegreesAngle() = (angle + Pi2) % Pi2 * radianToDegree

    fun clone(): PlayerAim = PlayerAim(angle, power)

    fun setSelectedFuel(selectedOption: FuelType?, elements: List<GuiMerchandise>, player: GamePlayer) {
        val newFuel = if (selectedFuel == selectedOption) null else selectedOption
        selectedFuel = newFuel
        val fuelBudget = player.cash // (-selectedShield.price), weapons purchase does not affect jump price
        elements.forEach {
            it.currentPhase = if (it.price > fuelBudget) GuiElementPhase.DISABLED else GuiElementPhase.IDLE
        }
    }

    fun addAngle(sign: Float = 1f) {
        angle += sign * precision * degreeToRadian
    }

    fun addPower(sign: Float = 1f) {
        power += sign * precision
    }

    fun addPrecision(sign: Float = 1f) {
        precision += sign * .4f
    }

    fun setPrecisionFromBar(value: Float) {
        precision = value.pow(2f).times(10f).coerceAtLeast(.01f)
    }

    fun getPrecisionForBar(): Float = precision.div(10f).pow(1f / 2f)

}
