package engine.freeBody

import engine.motion.Motion
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.collision.shapes.Shape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.World
import kotlin.math.PI
import kotlin.math.pow

open class FreeBody(
    val id: String,
    var motion: Motion,
    var shapeBox: Shape,
    var worldBody: Body,
    var radius: Float
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
                Vec2(
                    dx * worldBody.mass * 9.81f,
                    dy * worldBody.mass * 9.81f
                )
            )

            worldBody.applyAngularImpulse(dh * worldBody.inertia)
            return worldBody
        }

    }

}
