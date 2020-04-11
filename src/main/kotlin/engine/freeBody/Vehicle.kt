package engine.freeBody

import display.draw.TextureConfig
import display.graphic.BasicShapes
import engine.motion.Motion
import engine.shields.VehicleShield
import game.GamePlayer
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*
import utility.Common.makeVec2
import kotlin.math.PI
import kotlin.math.pow

class Vehicle(
    id: String,
    motion: Motion,
    worldBody: Body,
    radius: Float,
    textureConfig: TextureConfig
) : FreeBody(id, motion, worldBody, radius, textureConfig) {

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
            val fullShape = BasicShapes.polygon4Spiked.chunked(2)
            val bodyDef = createBodyDef(BodyType.DYNAMIC, x, y, h, dx, dy, dh)
            val worldBody = world.createBody(bodyDef)

            (listOf(fullShape.last()) + fullShape + listOf(fullShape.first()))
                .map { listOf(it[0] * radius, it[1] * radius) }
                .windowed(3, 2)
                .map { (a, b, c) ->
                    val shapeBox = PolygonShape()
                    val vertices = listOf(
                        makeVec2(a),
                        makeVec2(b),
                        makeVec2(c),
                        Vec2()
                    )
                        .toTypedArray()
                    shapeBox.set(vertices, vertices.size)
                    FixtureDef().also {
                        it.shape = shapeBox
                        it.density = mass / (PI.toFloat() * radius.pow(2f) * (fullShape.size * .5f))
                        it.friction = friction
                        it.restitution = restitution
                    }
                }
                .forEach { worldBody.createFixture(it) }

            textureConfig.chunkedVertices = listOf(listOf(0f, 0f)) + fullShape + listOf(fullShape.first())

            return Vehicle(player.name, Motion(), worldBody, radius, textureConfig)
                .also { player.vehicle = it }
        }

    }

}
