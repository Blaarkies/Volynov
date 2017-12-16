package backend.physics;

import backend.FreeBody;
import backend.UniversalConstants;
import com.sun.javafx.geom.Vec2d;

public class Gravity {

    // returns the weight force exerted on the client
    public static Vec2d gravitationalForce(FreeBody server, FreeBody client) {
        // F = (G*m*M)/(r^2)
        double G = UniversalConstants.gravitationalConstant;
        double m = client.mass;
        double M = server.mass;
        double r = client.motion.position.distance(server.motion.position);
        double forceOnClient = G * m * M / (r * r); // todo: use sqr() function(Math.pow(a,b) is very slow

        double direction = client.motion.position.getDirection(server.motion.position);

        double xF = forceOnClient * Math.sin(direction);
        double yF = forceOnClient * Math.cos(direction);

        return new Vec2d(xF, yF);
    }

}
