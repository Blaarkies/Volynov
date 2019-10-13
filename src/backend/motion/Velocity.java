package backend.motion;

import java.awt.geom.Point2D;

public class Velocity {

    // ddx is the derivative of x(the tempo of the change in position
    // ...how fast does it changes position)
    public double dx;
    public double dy;
    public double dh; //change in heading or rotationalSpeed

    public Velocity() {
        this.dx = 0;
        this.dy = 0;
        this.dh = 0;
    }

    public Velocity(double dx, double dy, double dh) {
        this.dx = dx;
        this.dy = dy;
        this.dh = dh;
    }

    public double getMagnitude() {
        return Point2D.distance(0, 0, dx, dy);
    }

    public double getDirection() {
        double theta = Math.atan2(dy, dx);
        theta = (theta < 0)
                ? theta + Math.PI * 2
                : theta;
        return theta;
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

