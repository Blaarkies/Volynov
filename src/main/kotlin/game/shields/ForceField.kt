package game.shields

import dI
import display.draw.TextureConfig
import display.graphic.Color
import engine.freeBody.Vehicle
import engine.freeBody.Warhead
import game.GamePlayer
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*
import org.jbox2d.dynamics.contacts.Contact
import org.jbox2d.dynamics.joints.WeldJointDef
import kotlin.math.pow

class ForceField(override val attachedTo: Vehicle) : VehicleShield {

    override val key = ShieldType.ForceField.toString()
    override lateinit var worldBody: Body
    override var energy = VehicleShield.defaultAmount
        set(value) {
            field = value
            if (field <= 0f) dispose()
        }
    override val radius = VehicleShield.defaultSize
    override lateinit var textureConfig: TextureConfig
    override val lastHits = mutableListOf<ShieldHit>()
    override val color = Color("#00968880")

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
        // detonate warhead
        dI.gameState.activeCallbacks.add { warhead.freeBodyCallback.callback(warhead, worldBody) }
        warhead.freeBodyCallback.isHandled = true
    }

    override fun blockDamage(amount: Float, firedBy: GamePlayer): Float {
        // Block a larger ratio of damage the more damage is inflicted
        // 20dmg = -15hp
        // 100dmg = -50hp
        // 200dmg = -78hp
        // 300dmg = -100hp

        if (energy <= 0f || firedBy == attachedTo.player) {
            // No shield, or own shield; do not block any damage
            return super.blockDamage(amount, firedBy)
        }

        if (lastHits.isEmpty() || dI.gameState.tickTime - lastHits.last().timeStamp > 100f) {
            // Add visual effect when not directly hit
            lastHits.add(ShieldHit(dI.gameState.tickTime, Vec2()))
        }

        val factor = 5f
        val gradient = .5f
        val translate = factor.pow(1f / gradient)

        val finalDamage = (((amount * 2f) + translate).pow(gradient)) * factor - factor.pow(1f / gradient)
        energy -= .4f * (1f - finalDamage / amount) * 100f + VehicleShield.defaultUsageCost

        return finalDamage
    }

}
