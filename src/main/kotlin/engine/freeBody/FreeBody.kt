package engine.freeBody

import display.draw.TextureConfig
import engine.motion.Motion
import org.jbox2d.collision.shapes.Shape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*
import kotlin.math.PI
import kotlin.math.pow

open class FreeBody(
    val id: String,
    var motion: Motion,
    var worldBody: Body,
    var radius: Float,
    val textureConfig: TextureConfig
) {

    companion object {

        fun createWorldBody(
            shapeBox: Shape,
            mass: Float,
            radius: Float,
            friction: Float,
            restitution: Float,
            world: World,
            bodyDef: BodyDef
        ): Body {
            val fixtureDef = FixtureDef().also {
                it.shape = shapeBox
                it.density = mass / (PI.toFloat() * radius.pow(2f))
                it.friction = friction
                it.restitution = restitution
            }

            return world.createBody(bodyDef)
                .also { it.createFixture(fixtureDef) }
        }

        fun createBodyDef(
            bodyType: BodyType,
            x: Float, y: Float, h: Float,
            dx: Float, dy: Float, dh: Float
        ): BodyDef {
            return BodyDef().also {
                it.type = bodyType
                it.position.set(x, y)
                it.angle = h
                it.linearVelocity = Vec2(dx, dy)
                it.angularVelocity = dh
            }
        }

    }

}
