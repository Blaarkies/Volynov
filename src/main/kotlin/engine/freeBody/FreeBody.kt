package engine.freeBody

import display.draw.TextureConfig
import engine.motion.Motion
import org.jbox2d.collision.shapes.Shape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*
import utility.Common.Pi
import utility.Common.makeVec2Circle
import kotlin.math.pow

open class FreeBody(val id: String, var radius: Float) {

    lateinit var worldBody: Body
    val motion = Motion()
    lateinit var textureConfig: TextureConfig

    fun knock(momentum: Float, direction: Float) {
        worldBody.applyLinearImpulse(makeVec2Circle(direction).mul(momentum / 9.81f), worldBody.position)
    }

    fun twist(momentum: Float) {
        worldBody.applyAngularImpulse(momentum / 9.81f)
    }

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
                it.density = mass / (Pi * radius.pow(2f))
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
