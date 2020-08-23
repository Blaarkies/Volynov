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
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.contacts.Contact
import org.jbox2d.dynamics.joints.MouseJoint
import org.jbox2d.dynamics.joints.MouseJointDef
import kotlin.math.pow

class ForceField(override val attachedTo: Vehicle) : VehicleShield {

    override lateinit var worldBody: Body
    override var energy = VehicleShield.defaultAmount
        set(value) {
            field = value
            if (field <= 0f) {
                dispose()
            }
        }
    override val radius = VehicleShield.defaultSize
    override val textureConfig: TextureConfig
    override val lastHits = mutableListOf<ShieldHit>()

    val color = Color("#00968880")

    init {
        val world = dI.gameState.world

        val bodyDef = BodyDef().also {
            it.type = BodyType.DYNAMIC
            it.position.set(attachedTo.worldBody.position)
        }

        val fixtureDef = FixtureDef().also {
            it.shape = CircleShape().also { shape -> shape.radius = radius }
            it.density = 100f
            it.friction = 0f
            it.restitution = 1f
        }

        worldBody = world.createBody(bodyDef).also {
            it.createFixture(fixtureDef)
            it.userData = this
        }
        setShieldEndTurn()

        MouseJointDef().also {
            it.target.set(attachedTo.worldBody.position)
            it.bodyA = attachedTo.worldBody
            it.bodyB = worldBody
            it.maxForce = worldBody.mass * 100f
            it.dampingRatio = .5f
            it.frequencyHz = 10f

            world.createJoint(it)
        }

        textureConfig = TextureConfig(TextureEnum.white_pixel, chunkedVertices = BasicShapes.polygon15.chunked(2),
            color = Color.TRANSPARENT)
            .also { it.updateGpuBufferData() }

    }

    override fun setShieldOnTurn() {
        val collisionFilter = worldBody.fixtureList.filterData
        collisionFilter.categoryBits = CollisionBits.onTurnShield // warhead does not hit own shield
        collisionFilter.maskBits = CollisionBits.warhead
    }

    override fun setShieldEndTurn() {
        val collisionFilter = worldBody.fixtureList.filterData
        collisionFilter.categoryBits = CollisionBits.shield
        collisionFilter.maskBits = CollisionBits.warhead
    }

    override fun update(tickTime: Float) {
        if (worldBody.jointList != null) {
            (worldBody.jointList.joint as MouseJoint).target = attachedTo.worldBody.position
        }

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

    override fun hit(warhead: Warhead, contact: Contact) {
        lastHits.add(
            ShieldHit(
                dI.gameState.tickTime,
                warhead.worldBody.position.sub(worldBody.position)))

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
