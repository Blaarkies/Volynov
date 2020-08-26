package game.shields

import dI
import display.draw.TextureConfig
import display.draw.TextureEnum
import display.graphic.Color
import display.graphic.vertex.BasicShapes
import engine.freeBody.Vehicle
import engine.freeBody.Warhead
import engine.physics.CollisionBits
import game.GamePlayer
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.contacts.Contact

interface VehicleShield {

    val key: String
    val color: Color
    val radius: Float
    var textureConfig: TextureConfig
    val lastHits: MutableList<ShieldHit>
    val attachedTo: Vehicle
    var worldBody: Body
    var energy: Float

    fun update(tickTime: Float) {
        if (lastHits.isEmpty()) {
            if (energy <= 0f) attachedTo.shield = null
            return
        }

        // TODO: use this to make multiple damage direction indicators
        val lastHit = lastHits.first()
        val timeStep = ((tickTime - lastHit.timeStamp) / 1000f).coerceIn(0f, 1f)
        textureConfig.color = color.setAlpha(1f - timeStep)
        textureConfig.updateGpuBufferData()

        if (timeStep == 1f) {
            lastHits.remove(lastHit)
        }
    }

    fun hit(warhead: Warhead, contact: Contact) {
        lastHits.add(
            ShieldHit(
                dI.gameState.tickTime,
                warhead.worldBody.position.sub(worldBody.position)))
    }

    fun setShieldOnTurn() {
        val collisionFilter = worldBody.fixtureList.filterData
        collisionFilter.categoryBits = CollisionBits.onTurnShield // warhead does not hit own shield
        collisionFilter.maskBits = CollisionBits.warhead
    }

    fun setShieldEndTurn() {
        val collisionFilter = worldBody.fixtureList.filterData
        collisionFilter.categoryBits = CollisionBits.shield
        collisionFilter.maskBits = CollisionBits.warhead
    }

    fun blockDamage(amount: Float, firedBy: GamePlayer) = amount

    fun setupDefaultShield() {
        worldBody = BodyDef().let {
            it.type = BodyType.DYNAMIC
            it.position.set(attachedTo.worldBody.position)
            dI.gameState.world.createBody(it)
        }
        worldBody.userData = this

        textureConfig = TextureConfig(TextureEnum.white_pixel,
            chunkedVertices = BasicShapes.polygon15.chunked(2),
            color = Color.TRANSPARENT)
            .also { it.updateGpuBufferData() }
    }

    fun dispose() = dI.gameState.world.destroyBody(worldBody)

    companion object {

        const val defaultUsageCost = 10f // base energy cost to power shields
        const val defaultSize = 1.2f // + 2f
        const val defaultAmount = 100f

        val descriptor = listOf(
            Pair(ShieldType.None, Descriptor(
                "No Shield",
                "An option for those who do not hide behind walls",
                0)
            { _ -> null }),
            Pair(ShieldType.ForceField, Descriptor(
                "Force Field",
                "Blocks a larger ratio of damage, the more damage is inflicted. Reduces inflicted damage by 23%-67%",
                400)
            { a -> ForceField(a) }),
            Pair(ShieldType.Deflector, Descriptor(
                "Deflector",
                "Bounces direct hits away without detonating them. Does not block any damage",
                600)
            { a -> Deflector(a) })
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
                         val factory: (Vehicle) -> VehicleShield?)

        fun create(selectedFuel: ShieldType?, vehicle: Vehicle): VehicleShield? =
            descriptor[selectedFuel]?.factory?.invoke(vehicle)
    }

}
