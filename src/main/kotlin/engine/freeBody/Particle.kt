package engine.freeBody

import display.draw.TextureConfig
import display.draw.TextureEnum
import display.graphic.vertex.BasicShapes
import display.graphic.Color
import engine.motion.Director.getDirection
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.World
import utility.Common
import utility.Common.Pi

class Particle(val id: String,
               particles: MutableList<Particle>,
               world: World,
               impacted: Body,
               location: Vec2,
               relativeVelocity: Vec2 = Vec2(),
               var radius: Float = 2f,
               private val duration: Float = 1000f,
               val texture: TextureEnum = TextureEnum.white_pixel,
               val color: Color = Color.WHITE,
               val createdAt: Float) {

    val worldBody: Body
    private var fullRadius = radius
    val textureConfig: TextureConfig

    init {
        val shapeBox = CircleShape()
        shapeBox.radius = radius

        val velocity = impacted.linearVelocity.add(relativeVelocity)
        val bodyDef = FreeBody.createBodyDef(BodyType.KINEMATIC,
            location.x, location.y, getDirection(relativeVelocity) + Pi * .5f,
            velocity.x, velocity.y, 0f)
        worldBody = world.createBody(bodyDef)

        textureConfig = TextureConfig(texture, chunkedVertices = BasicShapes.polygon30.chunked(2), color = color)
            .updateGpuBufferData()

        particles.add(this)
    }

    fun update(tickTime: Float, particles: MutableList<Particle>) {
        val ageTime = getAgeTime(tickTime)
        if (ageTime > duration) {
            particles.remove(this)
            return
        }

        val scale = Common.getTimingFunctionEaseOut(ageTime / duration)
        radius = fullRadius * scale
    }

    private fun getAgeTime(tickTime: Float) = tickTime - createdAt

}
