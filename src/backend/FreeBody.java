package backend;

import backend.motion.Motion;
import backend.motion.Velocity;
import com.sun.javafx.geom.Vec2d;

public class FreeBody implements UniversalConstants {

    public Motion motion;
    public double mass;
    public double angularMass;
    public double temperature;
    public double radius;

    public String id = "undefined";

    public FreeBody() {
        this.motion = new Motion();
        this.mass = 1;
        this.angularMass = getAngularMass(1,1);
        this.temperature = 298.15; // measured in Kelvin
        this.radius = 1;
        this.motion.position.setPosition(250, 250);
    }

    public FreeBody(double mass, double temperature, double radius,
                    double x, double y, double h,
                    double dx, double dy, double dh,
                    String id, int trailersPopulation) {
        this();
        this.mass = mass;
        this.angularMass = getAngularMass(mass,radius);
        this.temperature = temperature;
        this.radius = radius;
        this.motion = new Motion(x, y, h, dx, dy, dh, trailersPopulation);
        this.id = id;
    }

    public double getAngularMass(double mass, double area) {
        return mass * area; // todo: find real equation
    }

    // returns the weight force exerted on the client
    public Vec2d gravitationalForce(FreeBody client) {
        // F = (G*m*M)/(r^2)
        double G = UniversalConstants.gravitationalConstant;
        double m = client.mass;
        double M = this.mass;
        double r = client.motion.position.distance(this.motion.position);
        double forceOnClient = G*m*M / (r*r); // todo: use sqr() function(Math.pow(a,b) is very slow

        double direction = client.motion.position.getDirection(this.motion.position);

        double xF = forceOnClient * Math.sin(direction);
        double yF = forceOnClient * Math.cos(direction);

        return new Vec2d(xF, yF);
    }

    // returns the normal force exerted on the client
    public Vec2d collisionNormalForce(FreeBody client) {
        // todo: record the normalForce + direction for use by frictionTicks
        double buoyantForce = (this.radius+client.radius) - this.motion.position.distance(client.motion.position);
        Vec2d gravityVectorOnClient = gravitationalForce(client);

        Velocity vi = client.motion.velocity;
        double viMag = vi.getMagnitude();
        double viTheta = vi.getDirection();

        double FnTheta = this.motion.position.getDirection(client.motion.position); // direction from server
        double FnThetaInverse = FnTheta - Math.PI/2;

        double theta = FnThetaInverse - viTheta; // todo: test for all edge cases
        double Fn = viMag*Math.sin(theta) + buoyantForce*0.1;
        // buoyantForce is a phantom force! It corrects for the error caused by planet moving into the particle

        double xF = Fn * Math.sin(FnTheta) + gravityVectorOnClient.x;
        double yF = Fn * Math.cos(FnTheta) + gravityVectorOnClient.y;

        return new Vec2d(xF, yF);
    }


}
