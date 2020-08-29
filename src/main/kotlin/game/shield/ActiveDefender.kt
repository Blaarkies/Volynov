package game.shield

import dI
import display.draw.TextureConfig
import display.draw.light.LaserBeam
import display.graphic.Color
import engine.freeBody.Vehicle
import engine.freeBody.Warhead
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.contacts.Contact
import org.jbox2d.dynamics.joints.WeldJointDef
import utility.StopWatch

class ActiveDefender(override val attachedTo: Vehicle) : VehicleShield {

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
    override val color = Color("#ff33e680")

    private val stopWatch = StopWatch()
    private val laserBeams = HashMap<Warhead, LaserBeam>()

    init {
        setupDefaultShield()

        FixtureDef().also {
            it.shape = CircleShape().also { shape -> shape.radius = VehicleShield.laserSize }
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

    override fun update(tickTime: Float) {
        super.update(tickTime)

        laserBeams.entries.toList().forEach { (key, beam) ->
            beam.update(worldBody.position)
            if (!beam.active) laserBeams.remove(key)
        }
    }

    override fun render() {
        laserBeams.values.forEach {
            dI.textures.getTexture(it.texture).bind()
            dI.renderer.drawStrip(it.gpuData)
        }
    }

    override fun hit(warhead: Warhead, contact: Contact) {
        super.hit(warhead, contact)

        contact.isEnabled = false

        // laser the warhead
        if (stopWatch.elapsedTime < VehicleShield.laserInterval) return

        val deltaTime = stopWatch.elapsedTime.toFloat().coerceAtMost(VehicleShield.laserInterval * 1.5f)
        stopWatch.reset()

        val world = dI.gameState.world

        val hitList = mutableListOf<RayCastHit>()
        world.raycast({ fixture, point, normal, fraction ->
            hitList.add(RayCastHit(fixture, point, normal, fraction))
            1f
        }, attachedTo.worldBody.position, warhead.worldBody.position)

        hitList.filter {
            it.fixture.body.userData !is Vehicle
                    || it.fixture.body.userData !is VehicleShield
        }
            .minBy { it.fraction }!!
            .also {
                if (it.fixture.body == warhead.worldBody) {
                    val damage = VehicleShield.laserPower * deltaTime * .001f
                    warhead.sustainDamage(damage)

                    val targetVelocity = warhead.worldBody.linearVelocity
                    val beam = laserBeams[warhead]
                    if (beam != null) {
                        beam.update(worldBody.position, it.point, true, it.normal, targetVelocity)
                    } else {
                        laserBeams[warhead] = LaserBeam(worldBody.position, it.point, it.normal, targetVelocity)
                    }
                }
            }
    }

}

