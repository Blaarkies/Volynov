package engine.physics

import engine.FreeBody
import engine.motion.Force
import engine.motion.Velocity
import java.lang.Math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

object Contact {

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

}
