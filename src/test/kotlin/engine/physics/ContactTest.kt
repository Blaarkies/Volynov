package engine.physics

import engine.Planet
import engine.motion.Motion
import org.jbox2d.collision.Collision
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.World
import org.junit.jupiter.api.Test
//import org.jbox2d.*

import org.junit.jupiter.api.Assertions.*
import utilities.Utils

internal class ContactTest {

    @Test
    fun luna_terra_collision_should_bounce_in_relation_to_mass() {
        val terra = Planet("terra", .0, .0, .0, .0, .0, .0, 1000.0, radius = 20.0, restitution = 1.0)
        val luna = Planet("luna", -32.0, .0, .0, 1.0, .0, .0, 100.0, radius = 10.0, restitution = 1.0)
        val freeBodies = listOf(terra, luna)

        val terraStartSpeed = terra.motion.velocity.dx
        val lunaStartSpeed = luna.motion.velocity.dx
        val terraMass = terra.mass
        val lunaMass = luna.mass
        val totalStartMomentum = terraStartSpeed * terraMass + lunaStartSpeed * lunaMass

        tickPhysicsWithoutGravity(freeBodies) // did not touch yet

        assertEquals(terraStartSpeed, terra.motion.velocity.dx)
        assertEquals(lunaStartSpeed, luna.motion.velocity.dx)

        repeat(10) { tickPhysicsWithoutGravity(freeBodies) } // did touch now

        assertNotEquals(terraStartSpeed, terra.motion.velocity.dx)
        assertNotEquals(lunaStartSpeed, luna.motion.velocity.dx)

        val totalEndMomentum = terra.motion.velocity.dx * terra.mass + luna.motion.velocity.dx * luna.mass
        assertEquals(
            Utils.roundDouble(totalStartMomentum, 5),
            Utils.roundDouble(totalEndMomentum, 5),
            "Expected total momentum before and after collision to be equal"
        )

        assertTrue(
            terraStartSpeed - terra.motion.velocity.dx < lunaStartSpeed - luna.motion.velocity.dx,
            "Expected large body to change speed less than the small body"
        )
    }

    @Test
    fun luna_terra_glancing_collision_should_only_impart_a_small_force() {
        val terra = Planet("terra", .0, .0, .0, .0, .0, .0, 1000.0, radius = 20.0, restitution = 1.0)
        val luna = Planet("luna", -13.0, 29.0, .0, 1.0, .0, .0, 100.0, radius = 10.0, restitution = 1.0)
        val freeBodies = listOf(terra, luna)

        val lunaStartSpeed = luna.motion.velocity.magnitude

        tickPhysicsWithoutGravity(freeBodies) // did not touch yet

        assertEquals(lunaStartSpeed, luna.motion.velocity.magnitude)

        repeat(10) { tickPhysicsWithoutGravity(freeBodies) } // did touch now

        assertTrue(
            lunaStartSpeed > luna.motion.velocity.magnitude,
            "Expected Luna to lose a small amount of speed. " +
                    "Expected:${lunaStartSpeed}, but found:${luna.motion.velocity.magnitude}"
        )

    }

    @Test
    fun make_box_2_d_stuff() {
//        val subjectA: org.jbox2d.collision.shapes.CircleShape = CircleShape()
//        subjectA.radius = 200f
//
//        val subjectB: org.jbox2d.collision.shapes.CircleShape = CircleShape()
//        subjectB.radius = 200f
//

        var world = World(Vec2(-10f, 0f))
        var groundBodyDef = BodyDef()
        groundBodyDef.position.set(0f, 0f)

        var groundBody = world.createBody(groundBodyDef)
        var groundBox = PolygonShape()
        groundBox.setAsBox(50f, 10f)
        groundBody.createFixture(groundBox, 0f)


        var bodyDef = BodyDef()
        bodyDef.type = BodyType.DYNAMIC
        bodyDef.position.set(0f, 4f)
        var body = world.createBody(bodyDef)

        var dynamicBox = PolygonShape()
        dynamicBox.setAsBox(1f, 1f)

        var fixtureDef = FixtureDef()
        fixtureDef.shape = dynamicBox
        fixtureDef.density = 1f
        fixtureDef.friction = .3f

        body.createFixture(fixtureDef)

        var timeStep = 1f / 60f
        var velocityIterations = 6
        var positionIterations = 2

        repeat(60) {
            world.step(timeStep, velocityIterations, positionIterations)
            var position = body.position
            var angle = body.angle

            println("position x:${position.x}, y:${position.y}, angle:$angle")
        }


    }

    private fun tickPhysicsWithoutGravity(freeBodies: List<Planet>) {
        Motion.addLocationChanges(freeBodies)
        Contact.addCollisionForces(freeBodies)
        Motion.addVelocityChanges(freeBodies)
    }
}
