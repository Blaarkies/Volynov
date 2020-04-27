package engine.freeBody

import display.draw.TextureConfig
import display.draw.TextureEnum
import display.graphic.BasicShapes
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.World
import utility.Common

class Particle(
    val id: String,
    val worldBody: Body,
    var radius: Float,
    val textureConfig: TextureConfig,
    private val duration: Float
) {

    fun update(timeStep: Float, particles: MutableList<Particle>) {
        if (ageTime > duration) {
            particles.remove(this)
            return
        }

        worldBody.position.addLocal(worldBody.linearVelocity.mul(timeStep))

        val scale = Common.getTimingFunctionEaseOut(ageTime / duration)
        radius = fullRadius * scale
    }

    var fullRadius: Float = radius

    private val currentTime
        get() = System.currentTimeMillis()

    private val ageTime
        get() = (currentTime - createdAt)

    private val createdAt = currentTime

    companion object {

        fun createParticle(particles: MutableList<Particle>,
                           world: World,
                           impacted: Body,
                           location: Vec2,
                           radius: Float = 2f,
                           duration: Float = 1000f): Particle {
            val shapeBox = CircleShape()
            shapeBox.radius = radius

            val velocity = impacted.linearVelocity
            val bodyDef = FreeBody.createBodyDef(BodyType.STATIC, location.x, location.y, 0f,
                velocity.x, velocity.y, 0f)
            val worldBody = world.createBody(bodyDef)

            val textureConfig =
                TextureConfig(TextureEnum.white_pixel, chunkedVertices = BasicShapes.polygon30.chunked(2))
                    .updateGpuBufferData()
            return Particle("1", worldBody, shapeBox.radius, textureConfig, duration)
                .also { particles.add(it) }

        }

    }

}
