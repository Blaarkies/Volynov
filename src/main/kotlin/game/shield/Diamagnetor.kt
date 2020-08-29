package game.shield

import dI
import display.draw.TextureConfig
import display.graphic.Color
import engine.freeBody.Vehicle
import engine.freeBody.Warhead
import engine.motion.Director
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*
import org.jbox2d.dynamics.contacts.Contact
import org.jbox2d.dynamics.joints.WeldJointDef
import utility.Common.getTimingFunctionEaseOut
import utility.StopWatch

class Diamagnetor(override val attachedTo: Vehicle) : VehicleShield {

    override val key = ShieldType.Diamagnetor.toString()
    override lateinit var worldBody: Body
    override var energy = VehicleShield.defaultEnergyAmount
        set(value) {
            field = value
            if (field <= 0f) dispose()
        }
    override val radius = VehicleShield.defaultSize
    override lateinit var textureConfig: TextureConfig
    override val lastHits = mutableListOf<ShieldHit>()
    override val color = Color("#bcc8d380")

    private val stopWatch = StopWatch()

    init {
        setupDefaultShield()

        FixtureDef().also {
            it.shape = CircleShape().also { shape -> shape.radius = VehicleShield.magnetShieldSize }
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

        if (stopWatch.elapsedTime > VehicleShield.magnetInterval) {
            val deltaTime = stopWatch.elapsedTime
                .coerceAtMost((VehicleShield.magnetInterval * 1.5f).toLong())
            stopWatch.reset()

            val vectorToWarhead = warhead.worldBody.position.sub(worldBody.position)
            val normal = vectorToWarhead.clone().also { it.normalize() }
            val adjustedDistance = vectorToWarhead.length() - VehicleShield.defaultSize
            val distanceForce = VehicleShield.magnetShieldSize.minus(adjustedDistance)
                .div(VehicleShield.magnetShieldSize)
                .coerceAtLeast(0f)
                .let { getTimingFunctionEaseOut(it) }
            val impulse = distanceForce * warhead.worldBody.mass * VehicleShield.magnetPower *
                    deltaTime.div(VehicleShield.magnetInterval)

            warhead.knock(impulse, Director.getDirection(normal))
            energy -= impulse * VehicleShield.magnetEnergyFactor
        }
    }

}

