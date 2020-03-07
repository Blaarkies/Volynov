package engine.freeBody

import display.draw.TextureConfig
import engine.motion.Motion
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.collision.shapes.Shape
import org.jbox2d.dynamics.*
import kotlin.math.PI

class Planet(
    id: String,
    motion: Motion,
    shapeBox: Shape,
    worldBody: Body,
    radius: Float,
    textureConfig: TextureConfig
) : FreeBody(id, motion, shapeBox, worldBody, radius, textureConfig) {

    companion object {

        fun create(
            world: World,
            id: String,
            x: Float,
            y: Float,
            h: Float,
            dx: Float,
            dy: Float,
            dh: Float,
            mass: Float,
            radius: Float,
            restitution: Float = .5f,
            friction: Float = .3f,
            textureConfig: TextureConfig
        ): Planet {
            val shapeBox = CircleShape()
            shapeBox.radius = radius

            val bodyDef = createBodyDef(BodyType.DYNAMIC, x, y, h)
            val worldBody = createWorldBody(shapeBox, mass, radius, friction, restitution, world, bodyDef, dx, dy, dh)

            return Planet(id, Motion(), shapeBox, worldBody, radius, textureConfig)
        }

    }

}
