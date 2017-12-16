package backend;

import backend.motion.Motion;

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
        this.angularMass = getAngularMass(1, 1);
        this.temperature = 298.15; // Kelvin
        this.radius = 1;
        this.motion.position.setPosition(250, 250);
    }

    public FreeBody(double mass, double temperature, double radius,
                    double x, double y, double h,
                    double dx, double dy, double dh,
                    String id, int trailersPopulation) {
        this();
        this.mass = mass;
        this.angularMass = getAngularMass(mass, radius);
        this.temperature = temperature;
        this.radius = radius;
        this.motion = new Motion(x, y, h, dx, dy, dh, trailersPopulation);
        this.id = id;
    }

    public double getAngularMass(double mass, double area) {
        return mass * area; // todo: find real equation
    }

}
