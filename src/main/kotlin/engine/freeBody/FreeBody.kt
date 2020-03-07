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
    var shapeBox: Shape,
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
            bodyDef: BodyDef,
            dx: Float,
            dy: Float,
            dh: Float
        ): Body {
            val fixtureDef = FixtureDef()
            fixtureDef.shape = shapeBox
            fixtureDef.density = (mass / (PI.toFloat() * radius.pow(2)))
            fixtureDef.friction = friction
            fixtureDef.restitution = restitution

            val worldBody = world.createBody(bodyDef)
            worldBody.createFixture(fixtureDef)

            worldBody.applyForceToCenter(
                Vec2(dx, dy).mulLocal(worldBody.mass * 9.81f)
            )

            worldBody.applyAngularImpulse(dh * worldBody.inertia)
            return worldBody
        }

        fun createBodyDef(bodyType: BodyType, x: Float, y: Float, h: Float): BodyDef {
            val bodyDef = BodyDef()
            bodyDef.type = bodyType
            bodyDef.position.set(x, y)
            bodyDef.angle = h
            return bodyDef
        }

    }

}
