package game.shields

import dI
import display.draw.TextureConfig
import engine.freeBody.FreeBody
import engine.freeBody.Vehicle
import engine.freeBody.Warhead
import game.GamePlayer
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.contacts.Contact

interface VehicleShield {

    val radius: Float
    val textureConfig: TextureConfig

    val lastHits: MutableList<ShieldHit>
    fun update(tickTime: Float) {

    }

    fun hit(warhead: Warhead, contact: Contact) {

    }

    val attachedTo: FreeBody

    var worldBody: Body
    var energy: Float

    companion object {

        const val defaultUsageCost = 10f // base energy cost to power shields
        const val defaultSize = 1.2f // + 2f
        const val defaultAmount = 100f

        val descriptor = listOf(
            Pair(ShieldType.ForceField, Descriptor(
                "Force Field",
                "Blocks a larger ratio of damage, the more damage is inflicted. Reduces inflicted damage by 23%-67%",
                400)
            { a -> ForceField(a) })
        ).withIndex()
            .map { (index, item) ->
                val (_, descriptor) = item
                descriptor.order = index
                item
            }
            .toMap()

        class Descriptor(val name: String,
                         val description: String,
                         val price: Int = 0,
                         var order: Int = 0,
                         val factory: (Vehicle) -> VehicleShield)

        fun create(selectedFuel: ShieldType?, vehicle: Vehicle): VehicleShield =
            descriptor[selectedFuel]?.factory?.invoke(vehicle) ?: ForceField(vehicle)

    }

    fun setShieldOnTurn()
    fun setShieldEndTurn()
    fun blockDamage(amount: Float, firedBy: GamePlayer) = amount
    fun dispose() = dI.gameState.world.destroyBody(worldBody)

}
