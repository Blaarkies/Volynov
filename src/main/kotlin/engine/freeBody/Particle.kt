package engine.freeBody

import display.draw.TextureConfig
import display.draw.TextureEnum
import display.graphic.BasicShapes
import display.graphic.Color
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.World
import utility.Common

class Particle(val id: String,
               particles: MutableList<Particle>,
               world: World,
               impacted: Body,
               location: Vec2,
               var radius: Float = 2f,
               private val duration: Float = 1000f,
               val texture: TextureEnum = TextureEnum.white_pixel,
               val color: Color = Color.WHITE) {

    private val currentTime
        get() = System.currentTimeMillis()

    private val ageTime
        get() = (currentTime - createdAt)

    private val createdAt = currentTime
    val worldBody: Body
    private var fullRadius = radius
    val textureConfig: TextureConfig

    init {
        val shapeBox = CircleShape()
        shapeBox.radius = radius

        val velocity = impacted.linearVelocity
        val bodyDef = FreeBody.createBodyDef(BodyType.KINEMATIC, location.x, location.y, 0f,
            velocity.x, velocity.y, 0f)
        worldBody = world.createBody(bodyDef)

        textureConfig = TextureConfig(texture, chunkedVertices = BasicShapes.polygon30.chunked(2), color = color)
                .updateGpuBufferData()

        particles.add(this)
    }

    fun update(particles: MutableList<Particle>) {
        if (ageTime > duration) {
            particles.remove(this)
            return
        }

        val scale = Common.getTimingFunctionEaseOut(ageTime / duration)
        radius = fullRadius * scale
    }

}
