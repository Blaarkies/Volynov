package engine.freeBody

import display.draw.TextureConfig
import display.graphic.BasicShapes
import engine.motion.Motion
import engine.shields.VehicleShield
import game.GamePlayer
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.collision.shapes.Shape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*
import kotlin.math.PI
import kotlin.math.pow

class Vehicle(
    id: String,
    motion: Motion,
    shapeBox: Shape,
    worldBody: Body,
    radius: Float,
    textureConfig: TextureConfig
) : FreeBody(id, motion, shapeBox, worldBody, radius, textureConfig) {

    var shield: VehicleShield? = null
    var hitPoints: Float = 100f

    companion object {

        fun create(
            world: World,
            player: GamePlayer,
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
        ): Vehicle {
            val shapeBox = PolygonShape()
            val vertices = BasicShapes.squareStar.chunked(2)
                .map { Vec2(it[0] * radius, it[1] * radius) }
                .toTypedArray()
            shapeBox.set(vertices, vertices.size)

            val bodyDef = createBodyDef(BodyType.DYNAMIC, x, y, h, dx, dy, dh)
            val worldBody = createWorldBody(shapeBox, mass, radius, friction, restitution, world, bodyDef)



            val shapeBox2 = PolygonShape()
            val vertices2 = listOf(
                Vec2(0f, 1f),
                Vec2(-.5f, 0f),
                Vec2(0f, -1f),
                Vec2(.5f, 0f)
            )
                .toTypedArray()
            shapeBox2.set(vertices2, vertices2.size)
            val fixtureDef = FixtureDef().let {
                it.shape = shapeBox2
                it
            }
            worldBody.createFixture(fixtureDef)

            val shapeBox3 = PolygonShape()
            val vertices3 = listOf(
                Vec2(1f, 0f),
                Vec2(0f, -.5f),
                Vec2(-1f, 0f),
                Vec2(0f, .5f)
            )
                .toTypedArray()
            shapeBox3.set(vertices3, vertices3.size)
            val fixtureDef2 = FixtureDef().let {
                it.shape = shapeBox3
                it
            }
            worldBody.createFixture(fixtureDef2)


            textureConfig.chunkedVertices =
                shapeBox.vertices.map { listOf(it.x / radius, it.y / radius) }

            return Vehicle(player.name, Motion(), shapeBox, worldBody, radius, textureConfig)
                .let {
                    player.vehicle = it
                    it
                }
        }

    }

}
