package backend.physics;

import backend.FreeBody;
import backend.motion.Acceleration;
import backend.motion.Velocity;
import com.sun.javafx.geom.Vec2d;

public class Contact {

    // returns the normal force exerted on the client
    public static Vec2d contactNormalForce(FreeBody server, FreeBody client) {
        // todo: record the normalForce + direction for use by frictionTicks

        double CoRestitution = 0.8; // TODO: client.material ?
        double sinkDepth = (server.radius + client.radius
                - server.getDistance(client))
                / 3;
        sinkDepth = sinkDepth < 1 ? sinkDepth : 1;

        Acceleration acceleration = server.getRelativeAcceleration(client);
        Velocity velocity = server.getRelativeVelocity(client);

        Velocity totalVelocity = new Velocity(
                velocity.dx + acceleration.ddx,
                velocity.dy + acceleration.ddy,
                0);

        double FnTheta = server.getDirection(client);
        double FnThetaInverse = FnTheta - Math.PI / 2;
        double theta = FnThetaInverse - totalVelocity.getDirection();

        double FnVelocity = velocity.getMagnitude() * client.mass * Math.sin(theta)
                * CoRestitution;
        FnVelocity = FnVelocity < 0 ? 0 : FnVelocity;
        double FnAcceleration = acceleration.getMagnitude() * client.mass * Math.sin(theta)
                * sinkDepth;
        FnAcceleration = FnAcceleration < 0 ? 0 : FnAcceleration;

        double FnTotal = FnVelocity + FnAcceleration;
        double xF = FnTotal * Math.sin(FnTheta);
        double yF = FnTotal * Math.cos(FnTheta);

        return new Vec2d(xF, yF);
    }

}
