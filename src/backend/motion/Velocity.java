package backend.motion;

import java.awt.geom.Point2D;

public class Velocity {

    // ddx is the derivative of x(the tempo of the change in position
    // ...how fast does it changes position)
    public double dx;
    public double dy;
    public double dh; //change in heading or rotationalSpeed

    public Velocity() {
        setVelocity(0, 0, 0);
    }

    public Velocity(double dx, double dy, double dh) {
//        this();
        setVelocity(dx, dy, dh);
    }

    public void setVelocity(double dx, double dy, double dh) {
        this.dx = dx;
        this.dy = dy;
        this.dh = dh;
    }

    public double getMagnitude() {
        return Point2D.distance(0, 0, dx, dy);
    }

    public double getDirection() { // returns range [0..Pi] -> [-Pi..0]
        return Math.atan2(dx, dy);
    }

    //    inertial frame of reference ON the slow moving planet. How fast is the sat coming towards the planet?
    //    As fast as the planet, in the opposite direction as the planet (-dx) + the sat's velocity
    public Velocity getRelativeVelocity(Velocity client) {
        return new Velocity(-dx + client.dx, -dy + client.dy, -dh + client.dh);
    }

    public void addToVelocity(double dx, double dy, double dh) {
        this.dx += dx;
        this.dy += dy;
        this.dh += dh;
    }
}

