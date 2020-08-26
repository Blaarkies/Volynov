package game.shields

import dI
import display.draw.TextureConfig
import display.graphic.Color
import engine.freeBody.Vehicle
import engine.freeBody.Warhead
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.contacts.Contact
import org.jbox2d.dynamics.joints.MouseJoint
import org.jbox2d.dynamics.joints.MouseJointDef

class Deflector(override val attachedTo: Vehicle) : VehicleShield {

    override val key = ShieldType.Deflector.toString()
    override lateinit var worldBody: Body
    override var energy = VehicleShield.defaultAmount
        set(value) {
            field = value
            if (field <= 0f) dispose()
        }
    override val radius = VehicleShield.defaultSize
    override lateinit var textureConfig: TextureConfig
    override val lastHits = mutableListOf<ShieldHit>()
    override val color = Color("#2196f380")

    init {
        setupDefaultShield()

        FixtureDef().also {
            it.shape = CircleShape().also { shape -> shape.radius = radius }
            it.density = 100f
            it.friction = 0f
            it.restitution = 1f
            worldBody.createFixture(it)
        }

        setShieldEndTurn()

        MouseJointDef().also {
            it.target.set(attachedTo.worldBody.position)
            it.bodyA = attachedTo.worldBody
            it.bodyB = worldBody
            it.maxForce = worldBody.mass * 100f
            it.dampingRatio = .5f
            it.frequencyHz = 10f
            dI.gameState.world.createJoint(it)
        }
    }

    override fun update(tickTime: Float) {
        if (worldBody.jointList != null) {
            (worldBody.jointList.joint as MouseJoint).target = attachedTo.worldBody.position
        }

        super.update(tickTime)
    }

    override fun hit(warhead: Warhead, contact: Contact) {
        super.hit(warhead, contact)

        val body = warhead.worldBody
        dI.gameState.activeCallbacks.add {
            // Reduce spin speed on ricochets
            body.applyAngularImpulse(-body.angularVelocity * body.inertia * .8f)
        }

        energy -= 20f // Can bounce 5 shots
    }

}
