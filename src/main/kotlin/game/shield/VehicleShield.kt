package game.shield

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
import utility.Common

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

    fun render() {
        dI.textures.getTexture(textureConfig.texture).bind()
        dI.renderer.drawShape(
            textureConfig.gpuBufferData,
            worldBody.position,
            0f,
            Common.makeVec2(radius)
        )
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
        const val defaultSize = 1.2f
        const val defaultEnergyAmount = 100f
        const val magnetShieldSize = defaultSize + 1.5f
        const val magnetPower = 12f
        const val magnetInterval = 50f
        const val magnetEnergyFactor = .2f
        const val laserSize = 15f
        const val laserInterval = 16f
        const val laserPower = 200f // damage per second

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
                "Bounces direct hits away without detonating the warhead. Does not block any damage",
                600)
            { a -> Deflector(a) }),
            Pair(ShieldType.Diamagnetor, Descriptor(
                "Diamagnetor",
                "Pushes warheads away from without detonating them. Does not block any damage",
                900)
            { a -> Diamagnetor(a) }),
            Pair(ShieldType.Disintegrator, Descriptor(
                "Disintegrator",
                "Shatters nearby warheads without detonating them. Does not block any damage",
                1200)
            { a -> Disintegrator(a) }),
            Pair(ShieldType.Defender, Descriptor(
                "Active Defender",
                "Burns warheads at a distance with a laser until they detonate. Does not block any damage",
                1500)
            { a -> ActiveDefender(a) })
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
