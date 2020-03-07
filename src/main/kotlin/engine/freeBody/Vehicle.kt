package engine.freeBody

import display.draw.TextureConfig
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
            radius: Float = 13F,
            restitution: Float = .3f,
            friction: Float = .6f,
            textureConfig: TextureConfig
        ): Vehicle {
            val shapeBox = PolygonShape()
            val vertices = BasicShapes.polygon4.chunked(2).map { Vec2(it[0] * radius, it[1] * radius) }.toTypedArray()
            shapeBox.set(vertices, vertices.size)

            val bodyDef = createBodyDef(BodyType.DYNAMIC, x, y, h)
            val worldBody = createWorldBody(shapeBox, mass, radius, friction, restitution, world, bodyDef, dx, dy, dh)
            textureConfig.chunkedVertices =
                shapeBox.vertices.flatMap { listOf(it.x / radius, it.y / radius) }.chunked(2)

            return Vehicle(id, Motion(), shapeBox, worldBody, radius, textureConfig)
        }

    }

}
