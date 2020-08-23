package game

import display.gui.base.GuiElementPhase
import display.gui.elements.GuiMerchandise
import game.fuel.Fuel
import game.fuel.FuelType
import game.shields.ShieldType
import game.shields.VehicleShield
import utility.Common.Pi2
import utility.Common.degreeToRadian
import utility.Common.radianToDegree
import java.lang.Integer.max
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

    var precision = precisionMax - 1f
        set(value) {
            field = value.coerceIn(0f, precisionMax - precisionMin)
        }

    var selectedFuel: FuelType? = null
    var selectedShield: ShieldType? = null
    var selectedWeapon: Boolean? = null

    val selectedFuelDescriptor: Fuel.Companion.Descriptor?
        get() = Fuel.descriptor[selectedFuel]
    val selectedShieldDescriptor: VehicleShield.Companion.Descriptor?
        get() = VehicleShield.descriptor[selectedShield]
    val selectedWeaponDescriptor: Fuel.Companion.Descriptor?
        get() = null

    fun getDegreesAngle() = (angle + Pi2) % Pi2 * radianToDegree

    fun clone(): PlayerAim = PlayerAim(angle, power)

    fun setSelectedFuel(selectedOption: FuelType?, elements: List<GuiMerchandise>, player: GamePlayer) {
        val newFuel = if (selectedFuel == selectedOption) null else selectedOption
        selectedFuel = newFuel
        val shieldInCartPrice = selectedShieldDescriptor?.price ?: 0
        val fuelBudget = player.cash - shieldInCartPrice //, weapons purchase does not affect jump price
        elements.forEach {
            it.currentPhase = if (it.price > fuelBudget) GuiElementPhase.DISABLED else GuiElementPhase.IDLE
        }
    }

    fun setSelectedShield(selectedOption: ShieldType?, elements: List<GuiMerchandise>, player: GamePlayer) {
        val newShield = if (selectedShield == selectedOption) null else selectedOption
        selectedShield = newShield
        val fuelInCartPrice = selectedFuelDescriptor?.price ?: 0
        val weaponInCartPrice = selectedWeaponDescriptor?.price ?: 0

        val shieldBudget = player.cash - max(weaponInCartPrice, fuelInCartPrice)
        val existingShield = player.vehicle?.shield
        val existingShieldIsPristine = existingShield?.energy == VehicleShield.defaultAmount
        elements.forEach {
            val notAvailable = it.price > shieldBudget
//                    || (existingShieldIsPristine && it.name == existingShield.name)
            it.currentPhase = if (notAvailable) GuiElementPhase.DISABLED else GuiElementPhase.IDLE
        }
    }

    fun addAngle(sign: Float = 1f) {
        angle += sign * (precisionMax - precision) * degreeToRadian
    }

    fun addPower(sign: Float = 1f) {
        power += sign * (precisionMax - precision)
    }

    fun addPrecision(sign: Float = 1f) {
        precision += sign * .1f
    }

    fun setPrecisionFromBar(value: Float) {
        precision = value.pow(1f / precisionExponent).times(precisionMax).coerceAtMost(precisionMax - precisionMin)
    }

    fun getPrecisionForBar(): Float = precision.div(precisionMax).pow(precisionExponent)

    companion object {

        private const val precisionMin = .01f
        private const val precisionMax = 10f
        private const val precisionExponent = 8f

    }
}
