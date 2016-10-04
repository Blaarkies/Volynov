package backend;

import backend.motion.Motion;

public class FreeBody {
    public Motion motion;
    public double mass;
    public double angularMass;
    public double temperature;
    public double area;

    public FreeBody() {
        this.motion = new Motion();
        this.mass = 1;
        this.angularMass = getAngularMass(1,1);
        this.temperature = 298.15;
        this.area = 1;

        this.motion.position.setPosition(250, 250);
    }

    public FreeBody(double mass, double temperature, double area) {
        this();
        this.mass = mass;
        this.angularMass = getAngularMass(mass,area);
        this.temperature = temperature;
        this.area = area;

        this.motion.position.setPosition(250, 250); // todo:
    }

    public double getAngularMass(double mass, double area) {
        return mass * area; // todo: find real equation
    }
}
