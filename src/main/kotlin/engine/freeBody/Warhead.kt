package engine.freeBody

import display.draw.TextureConfig
import display.graphic.BasicShapes
import engine.motion.Motion
import game.GamePlayer
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.collision.shapes.Shape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.World

class Warhead(
    id: String,
    firedBy: GamePlayer,
    motion: Motion,
    shapeBox: Shape,
    worldBody: Body,
    radius: Float,
    textureConfig: TextureConfig
) : FreeBody(id, motion, shapeBox, worldBody, radius, textureConfig) {


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
            textureConfig: TextureConfig
        ): Warhead {
            val shapeBox = PolygonShape()
            val vertices = BasicShapes.polygon4.chunked(2)
                .map { Vec2(it[0] * radius * 2f, it[1] * radius) }
                .toTypedArray()
            shapeBox.set(vertices, vertices.size)

            val bodyDef = createBodyDef(BodyType.DYNAMIC, x, y, h, dx, dy, dh)
            val worldBody = createWorldBody(shapeBox, mass, radius, friction, restitution, world, bodyDef)
            textureConfig.chunkedVertices =
                shapeBox.vertices.map { listOf(it.x / radius, it.y / radius) }

            return Warhead("1", firedBy, Motion(), shapeBox, worldBody, radius, textureConfig)
                .let {
//                    it.textureConfig.updateGpuBufferData()
                    it.textureConfig.gpuBufferData = it.textureConfig.chunkedVertices.flatMap {
                        val (x, y) = it
                        listOf(
                            x, y, 0f,
                            1f, .3f, .3f, 1f,
                            (x * .5f - 0.5f) * .100f,
                            (y * .5f - 0.5f) * .100f
                        )
                    }.toFloatArray()

                    firedBy.warheads.add(it)
                    it
                }
        }

    }

}
