package backend.physics;

import backend.FreeBody;
import backend.motion.Acceleration;
import backend.motion.ContactEvent;
import backend.motion.Force;
import backend.motion.Velocity;
import com.sun.javafx.geom.Vec2d;

import java.util.List;

public class Contact {

    // returns the normal force exerted on the client
    public static Vec2d contactNormalForce(FreeBody server, FreeBody client) {
        // todo: record the normalForce + direction for use by frictionTicks

        double CoRestitution = 1;//0.8; // TODO: client.material ?
        // CoRestitution < 1 causes sink issues

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
        double thetaV = totalVelocity.getDirection();
        double thetaDiff = FnThetaInverse - thetaV;

        double FnV = velocity.getMagnitude() * client.mass * Math.sin(thetaDiff)
                * CoRestitution;
        FnV = FnV < 0 ? 0 : FnV;
        double FnAcc = acceleration.getMagnitude() * client.mass * Math.sin(thetaDiff)
                * sinkDepth;
        FnAcc = FnAcc < 0 ? 0 : FnAcc;

        double FnTotal = FnV + FnAcc;
        double xF = FnTotal * Math.cos(FnTheta);
        double yF = FnTotal * Math.sin(FnTheta);

        return new Vec2d(xF, yF);
    }

    public static void frictionForce(FreeBody client) {
        double uKinetic = 0.5;

        List<ContactEvent> contactEvents = client.motion.contactEvents;
        for (ContactEvent contactEvent : contactEvents) {
            Velocity velocity = client.getRelativeVelocity(contactEvent.server);
            Force normalForce = contactEvent.normalForce; // by server upon client
            double frictionDirection;

            double v = modulateDirection(velocity.getDirection());
            double fn = modulateDirection(normalForce.getDirection());
            double theta = fn - v;
            theta = modulateDirection(theta);

            frictionDirection = (theta < Math.PI)
                    ? normalForce.getDirection() - (Math.PI / 2)
                    : normalForce.getDirection() + (Math.PI / 2);

            double accOnClient = uKinetic * normalForce.getMagnitude() / client.mass;
            if (accOnClient > velocity.getMagnitude()) {
                accOnClient = velocity.getMagnitude();
            }
            double xAcc = accOnClient * Math.cos(frictionDirection);
            double yAcc = accOnClient * Math.sin(frictionDirection);

            client.motion.acceleration.addToAcceleration(
                    xAcc,
                    yAcc,
                    0);
        }
        contactEvents.clear();

    }

    private static double modulateDirection(double theta) {
        if (theta < 0) {
            theta = theta + Math.abs(Math.floor(theta / (Math.PI * 2))) * (Math.PI * 2);
        }
        theta = theta % (Math.PI * 2);
        return theta;
    }

}
