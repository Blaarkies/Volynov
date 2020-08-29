package game.shield

import dI
import display.draw.TextureConfig
import display.draw.TextureEnum
import display.graphic.Color
import engine.freeBody.Particle
import engine.freeBody.Vehicle
import engine.freeBody.Warhead
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.contacts.Contact
import org.jbox2d.dynamics.joints.WeldJointDef

class Disintegrator(override val attachedTo: Vehicle) : VehicleShield {

    override val key = ShieldType.Disintegrator.toString()
    override lateinit var worldBody: Body
    override var energy = VehicleShield.defaultEnergyAmount
        set(value) {
            field = value
            if (field <= 0f) dispose()
        }
    override val radius = VehicleShield.defaultSize
    override lateinit var textureConfig: TextureConfig
    override val lastHits = mutableListOf<ShieldHit>()
    override val color = Color("#7e213680")

    init {
        setupDefaultShield()

        FixtureDef().also {
            it.shape = CircleShape().also { shape -> shape.radius = radius }
            it.density = .00000001f
            it.friction = 0f
            it.restitution = 1f
            worldBody.createFixture(it)
        }

        setShieldEndTurn()

        WeldJointDef().also {
            it.initialize(attachedTo.worldBody, worldBody, Vec2())
            dI.gameState.world.createJoint(it)
        }
    }

    override fun hit(warhead: Warhead, contact: Contact) {
        super.hit(warhead, contact)

        contact.isEnabled = false
        // disintegrate warhead
        disintegrate(warhead)
        warhead.freeBodyCallback.isHandled = true

        energy -= 20f
    }

    private fun disintegrate(warhead: Warhead) {
        dI.gameState.activeCallbacks.add {
            Particle("warhead_dust", dI.gameState.particles, dI.gameState.world, worldBody.linearVelocity,
                worldBody.position,
                worldBody.linearVelocity, 1f, 1000f, TextureEnum.rcs_puff, createdAt = dI.gameState.tickTime)

            warhead.dispose(dI.gameState.world, dI.gameState.warheads)
        }
    }

}

