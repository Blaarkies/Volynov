package engine.freeBody

import engine.motion.Motion
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.collision.shapes.Shape
import org.jbox2d.dynamics.*

class Planet(
    id: String,
    motion: Motion,
    shapeBox: Shape,
    worldBody: Body,
    radius: Float
) : FreeBody(id, motion, shapeBox, worldBody, radius) {

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
            friction: Float = .3f
        ): Planet {
            val bodyDef = BodyDef()
            bodyDef.type = BodyType.DYNAMIC
            bodyDef.position.set(x, y)
            val shapeBox = CircleShape()
            shapeBox.radius = radius

            // TODO: create object pre-rotated with "h" angle
            val worldBody = createWorldBody(shapeBox, mass, radius, friction, restitution, world, bodyDef, dx, dy, dh)

            return Planet(id, Motion(), shapeBox, worldBody, radius)
        }
    }

}
