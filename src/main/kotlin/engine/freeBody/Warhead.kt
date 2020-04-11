package engine.freeBody

import display.draw.TextureConfig
import display.draw.TextureEnum
import display.graphic.BasicShapes
import engine.FreeBodyCallback
import engine.motion.Motion
import game.GamePlayer
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.World

class Warhead(
    id: String,
    val firedBy: GamePlayer,
    motion: Motion,
    worldBody: Body,
    radius: Float,
    textureConfig: TextureConfig
) : FreeBody(id, motion, worldBody, radius, textureConfig) {

    private val currentTime
        get() = System.currentTimeMillis()

    val ageTime
        get() = (currentTime - createdAt)

    private val createdAt = currentTime
    val selfDestructTime = 45000f
    // TODO: player in current aiming phase could just wait out this time if they wanted to

    val damage = 100f

    fun createParticles(
        particles: MutableList<Particle>,
        world: World,
        impacted: Body
    ): Particle {
        val shapeBox = CircleShape()
        shapeBox.radius = 2f

        val location = worldBody.position
        val velocity = impacted.linearVelocity
        val bodyDef = createBodyDef(BodyType.STATIC, location.x, location.y, 0f, velocity.x, velocity.y, 0f)
        val worldBody = world.createBody(bodyDef)
//            createWorldBody(shapeBox, 0f, radius, 0f, 0f, world, bodyDef)

        val textureConfig = TextureConfig(TextureEnum.white_pixel, chunkedVertices = BasicShapes.polygon30.chunked(2))
            .updateGpuBufferData()
        return Particle(id, worldBody, shapeBox.radius, textureConfig)
            .also { particles.add(it) }

    }

    companion object {

        fun create(
            world: World,
            firedBy: GamePlayer,
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
            textureConfig: TextureConfig,
            onWarheadCollision: (FreeBody, Body) -> Unit
        ): Warhead {
            val shapeBox = PolygonShape()
            val vertices = BasicShapes.polygon4.chunked(2)
                .map { Vec2(it[0] * radius * 2f, it[1] * radius) }
                .toTypedArray()
            shapeBox.set(vertices, vertices.size)

            val bodyDef = createBodyDef(BodyType.DYNAMIC, x, y, h, dx, dy, dh)
            val worldBody = createWorldBody(shapeBox, mass, radius, friction, restitution, world, bodyDef)
            worldBody.isBullet = true

            textureConfig.chunkedVertices =
                shapeBox.vertices.map { listOf(it.x / radius, it.y / radius) }

            return Warhead("1", firedBy, Motion(), worldBody, radius, textureConfig)
                .also {
//                    it.textureConfig.updateGpuBufferData()
                    it.textureConfig.gpuBufferData = it.textureConfig.chunkedVertices.flatMap {
                        val (x, y) = it
                        listOf(
                            x, y, 0f,
                            1f, .3f, .3f, 1f,
                            (x * .5f - 0.5f) * .1f,
                            (y * .5f - 0.5f) * .1f
                        )
                    }.toFloatArray()

                    firedBy.warheads.add(it)
                    worldBody.userData = FreeBodyCallback(it, onWarheadCollision)
                }
        }

    }

}
