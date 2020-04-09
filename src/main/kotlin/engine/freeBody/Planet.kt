package engine.freeBody

import display.draw.TextureConfig
import engine.motion.Motion
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.dynamics.*

class Planet(
    id: String,
    motion: Motion,
    worldBody: Body,
    radius: Float,
    textureConfig: TextureConfig
) : FreeBody(id, motion, worldBody, radius, textureConfig) {

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

            val bodyDef = createBodyDef(BodyType.DYNAMIC, x, y, h, dx, dy, dh)
            val worldBody = createWorldBody(shapeBox, mass, radius, friction, restitution, world, bodyDef)

            return Planet(id, Motion(), worldBody, radius, textureConfig)
        }

    }

}
