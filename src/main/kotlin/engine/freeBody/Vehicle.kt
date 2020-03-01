package engine.freeBody

import display.graphic.BasicShapes
import engine.motion.Motion
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.collision.shapes.Shape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*

class Vehicle(
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
            radius: Float = 13F,
            restitution: Float = .3f,
            friction: Float = .6f
        ): Vehicle {
            val bodyDef = BodyDef()
            bodyDef.type = BodyType.DYNAMIC
            bodyDef.position.set(x, y)
            val shapeBox = PolygonShape()
            val vertices = BasicShapes.polygon5.chunked(2).map { Vec2(it[0]*radius, it[1]*radius) }.toTypedArray()
            shapeBox.set(vertices, vertices.size)

            val worldBody = createWorldBody(shapeBox, mass, radius, friction, restitution, world, bodyDef, dx, dy, dh)

            return Vehicle(id, Motion(), shapeBox, worldBody, radius)
        }

    }

}
