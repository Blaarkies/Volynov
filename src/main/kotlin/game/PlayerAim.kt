package game

import display.gui.base.GuiElementPhase.IDLE
import display.gui.base.GuiElementPhase.DISABLED
import display.gui.special.MerchandiseLists
import game.fuel.Fuel
import game.fuel.FuelType
import game.shield.ShieldType
import game.shield.VehicleShield
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

    val selectedFuelDescriptor: Fuel.Companion.Descriptor
        get() = Fuel.descriptor[selectedFuel] ?: Fuel.descriptor[FuelType.Hydrazine]!!
    val selectedShieldDescriptor: VehicleShield.Companion.Descriptor?
        get() = VehicleShield.descriptor[selectedShield]
    val selectedWeaponDescriptor: Fuel.Companion.Descriptor?
        get() = null

    fun getDegreesAngle() = (angle + Pi2) % Pi2 * radianToDegree

    fun clone(): PlayerAim = PlayerAim(angle, power)

    fun setSelectedWeapon(selectedOption: FuelType?, merchandise: MerchandiseLists, player: GamePlayer) {


        updateAvailableMerchandise(merchandise, player)
    }

    fun setSelectedShield(selectedOption: ShieldType?, merchandise: MerchandiseLists, player: GamePlayer) {
        val newShield = if (selectedShield == selectedOption) null else selectedOption
        selectedShield = newShield

        updateAvailableMerchandise(merchandise, player)
    }

    fun setSelectedFuel(selectedOption: FuelType?, merchandise: MerchandiseLists, player: GamePlayer) {
        val newFuel = if (selectedFuel == selectedOption) null else selectedOption
        selectedFuel = newFuel

        updateAvailableMerchandise(merchandise, player)
    }

    private fun updateAvailableMerchandise(merchandise: MerchandiseLists, player: GamePlayer) {
        val weaponInCartPrice = selectedWeaponDescriptor?.price ?: 0
        val shieldInCartPrice = selectedShieldDescriptor?.price ?: 0
        val fuelInCartPrice = selectedFuelDescriptor.price

        val fuelBudget = player.cash - shieldInCartPrice // weapons purchase does not affect jump price
        merchandise.fuels.forEach {
            it.currentPhase = if (it.price > fuelBudget) DISABLED else IDLE
        }

        val shieldBudget = player.cash - max(weaponInCartPrice, fuelInCartPrice)
        val existingShield = player.vehicle?.shield
        val existingShieldIsPristine = existingShield?.energy == VehicleShield.defaultEnergyAmount
        merchandise.shields.forEach {
            val notAvailable = it.price > shieldBudget
                    || (existingShieldIsPristine && it.key == existingShield?.key)
            it.currentPhase = if (notAvailable) DISABLED else IDLE
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
