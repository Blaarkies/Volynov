package engine.freeBody

import display.draw.TextureConfig
import display.draw.TextureEnum
import display.graphic.BasicShapes
import display.graphic.Color
import engine.FreeBodyCallback
import engine.motion.Motion
import game.GamePlayer
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.World

class Warhead(
    id: String,
    warheads: MutableList<Warhead>,
    world: World,
    val firedBy: GamePlayer,
    x: Float,
    y: Float,
    h: Float,
    dx: Float,
    dy: Float,
    dh: Float,
    mass: Float,
    radius: Float = .7F,
    restitution: Float = .3f,
    friction: Float = .6f,
    onCollision: (FreeBody, Body) -> Unit
) : FreeBody(id, radius) {

    init {
        val shapeBox = PolygonShape()
        val vertices = BasicShapes.polygon4.chunked(2)
            .map { Vec2(it[0] * radius * 2f, it[1] * radius) }
            .toTypedArray()
        shapeBox.set(vertices, vertices.size)

        val bodyDef = createBodyDef(BodyType.DYNAMIC, x, y, h, dx, dy, dh)
        worldBody = createWorldBody(shapeBox, mass, radius, friction, restitution, world, bodyDef)
        worldBody.isBullet = true
        worldBody.userData = FreeBodyCallback(this, onCollision)

        textureConfig = TextureConfig(TextureEnum.metal,
            chunkedVertices = shapeBox.vertices.map { listOf(it.x / radius, it.y / radius) },
            color = Color.createFromHsv(0f, 1f, .3f, 1f))
            .updateGpuBufferData()

        firedBy.warheads.add(this)
        warheads.add(this)
    }

    private val currentTime
        get() = System.currentTimeMillis()

    val ageTime
        get() = (currentTime - createdAt)

    private val createdAt = currentTime
    val selfDestructTime = 45000f
    // TODO: player in current aiming phase could just wait out this time if they wanted to
    // also influences score

    val damage = 50f
    val energy = 50f

    var lastGravityForce: Float = 0f
    val isOutOfGravityField: Boolean
        get() {
            val nowGravityForce = worldBody.m_force.length()
            return nowGravityForce < 0.02f
        }

    fun updateLastGravityForce() {
        lastGravityForce = worldBody.m_force.length()
    }

}
