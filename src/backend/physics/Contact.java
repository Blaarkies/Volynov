package backend.physics;

import backend.FreeBody;
import backend.motion.Acceleration;
import backend.motion.Velocity;
import com.sun.javafx.geom.Vec2d;

public class Contact {

    // returns the normal force exerted on the client
    public static Vec2d contactNormalForce(FreeBody server,FreeBody client) {
        // todo: record the normalForce + direction for use by frictionTicks

        double CoRestitution = 1; // TODO: client.material ?

        Acceleration acceleration = server.motion.acceleration.getRelativeAcceleration(client.motion.acceleration);
        Velocity velocity = server.motion.velocity.getRelativeVelocity(client.motion.velocity);

        Velocity totalVelocity = new Velocity(
                velocity.dx + acceleration.ddx,
                velocity.dy + acceleration.ddy,
                0);

        double FnTheta = server.motion.position.getDirection(client.motion.position);
        double FnThetaInverse = FnTheta - Math.PI / 2;

        double theta = FnThetaInverse - totalVelocity.getDirection(); // todo: test for all edge cases
        double Fn = CoRestitution
                * (totalVelocity.getMagnitude() * client.mass * Math.sin(theta));

        double xF = Fn * Math.sin(FnTheta);
        double yF = Fn * Math.cos(FnTheta);

        return new Vec2d(xF, yF);
    }

}
