package game

import display.gui.base.GuiElementPhase
import display.gui.elements.GuiMerchandise
import game.fuel.Fuel
import game.fuel.FuelType
import utility.Common.Pi2
import utility.Common.radianToDegree

class PlayerAim(var angle: Float = 0f, power: Float = 100f) {

    var power = power
        set(value) {
            field = value.coerceIn(0f, 100f)
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

}
