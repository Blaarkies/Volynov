package engine.physics

import engine.FreeBody
import engine.motion.Acceleration
import engine.motion.Velocity
import utilities.Utils
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object Contact {

    fun addCollisionForces(freeBodies: List<FreeBody>) {
        Utils.joinListsNoDuplicate(freeBodies, freeBodies)
            .filter { (a, b) -> a.getDistance(b) <= a.radius + b.radius }
            .forEach { (server, client) ->
                val restitution = server.restitution * client.restitution

                val (normalDirection, surfaceDirection) = client.getDirection(server)
                    .let { Pair(it, it + PI / 2) }
                val (relativeSpeed, velocityDirection) = server.getRelativeVelocity(client)
                    .let { Pair(it.magnitude, it.direction) }

                // rotate reference so that x is parallel to the surface of the collision
                val angleVelocityToSurface = velocityDirection - surfaceDirection
                // collision only normal force imparts zero x component. y is determined by angleVelocityToSurface
                val velocityNormalY = (sin(angleVelocityToSurface) * relativeSpeed)
                if (velocityNormalY < 0) {
                    return@forEach
                }
                // rotate x,y pair by angle=(-surfaceDirection) back to original reference
                val velocityAngled = (angleVelocityToSurface).let { referenceDirection ->
                    Velocity(
                        -velocityNormalY * sin(referenceDirection),
                        velocityNormalY * cos(referenceDirection)
                    )
                }

                val totalMass = server.mass + client.mass
                val finalVelocity = Velocity(
                    (server.mass * server.motion.velocity.dx + client.mass * client.motion.velocity.dx) / totalMass,
                    (server.mass * server.motion.velocity.dy + client.mass * client.motion.velocity.dy) / totalMass
                )
                    .let { perfectInelasticVelocity -> velocityAngled.getRelative(perfectInelasticVelocity) }
                    .let { relativeToCombinedVelocity ->
                        Velocity(
                            relativeToCombinedVelocity.dx * (1 - client.mass / totalMass) * (1 + restitution),
                            relativeToCombinedVelocity.dy * (1 - client.mass / totalMass) * (1 + restitution)
                        )
                    }

                client.motion.acceleration.add(
                    Acceleration(cos(normalDirection) * finalVelocity.dx, sin(normalDirection) * finalVelocity.dy)
                )

//                client.motion.acceleration.add(
//                    Acceleration(finalVelocity.dx, finalVelocity.dy)
//                )

//                val actualDistance = server.getDistance(client)
//                val expectedDistance = server.radius + client.radius
//                val fixDistance = (expectedDistance - actualDistance).coerceAtLeast(.0)
//                if (fixDistance > .0) {
//                    client.motion.location.add(cos(normalDirection) * fixDistance, sin(normalDirection) * fixDistance)
//                }
            }
    }

    /*
    // returns the normal force exerted on the client
    fun contactNormalForce(server: FreeBody, client: FreeBody): Force {
        // TODO: record the normalForce + direction for use by frictionTicks

        val coefRestitution = 1.0//0.8; // TODO: client.material ?
        // TODO: CoRestitution < 1 causes sink issues

        var sinkDepth = (server.radius + client.radius - server.getDistance(client)) / 3
        sinkDepth = if (sinkDepth < 1) sinkDepth else 1.0

        val acceleration = server.getRelativeAcceleration(client)
        val velocity = server.getRelativeVelocity(client)

        val totalVelocity = Velocity(
            velocity.dx + acceleration.ddx,
            velocity.dy + acceleration.ddy,
            0.0
        )

        val FnTheta = server.getDirection(client)
        val FnThetaInverse = FnTheta - PI / 2
        val thetaV = totalVelocity.direction
        val thetaDiff = FnThetaInverse - thetaV

        var FnV = (velocity.magnitude * client.mass * sin(thetaDiff)
                * coefRestitution)
        FnV = if (FnV < 0) 0.0 else FnV
        var FnAcc = (acceleration.magnitude * client.mass * sin(thetaDiff)
                * sinkDepth)
        FnAcc = if (FnAcc < 0) 0.0 else FnAcc

        val FnTotal = FnV + FnAcc
        val xF = FnTotal * cos(FnTheta)
        val yF = FnTotal * sin(FnTheta)

        return Force(xF, yF)
    }

    fun frictionForce(client: FreeBody) {
        val uKinetic = 0.2

        val contactEvents = client.motion.contactEvents
        for (contactEvent in contactEvents) {
            val velocity = client.getRelativeVelocity(contactEvent.server)
            val normalForce = contactEvent.normalForce // by server upon client
            val frictionDirection: Double

            val v = modulateDirection(velocity.direction)
            val fn = modulateDirection(normalForce.direction)
            var theta = fn - v
            theta = modulateDirection(theta)

            frictionDirection = if (theta < PI)
                normalForce.direction - PI / 2
            else
                normalForce.direction + PI / 2

            var accOnClient = uKinetic * normalForce.magnitude / client.mass
            if (accOnClient > velocity.magnitude) {
                accOnClient = velocity.magnitude
            }
            val xAcc = accOnClient * cos(frictionDirection)
            val yAcc = accOnClient * sin(frictionDirection)

            client.motion.acceleration.add(
                xAcc,
                yAcc,
                0.0
            )
        }
        contactEvents.clear()

    }

    private fun modulateDirection(theta: Double): Double {
        return when {
            theta < 0 -> abs(floor(theta / (PI * 2))) * (PI * 2)
            else -> theta
        } % (PI * 2)
    }
    */


}
