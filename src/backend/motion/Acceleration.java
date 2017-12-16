package backend.motion;

import java.awt.geom.Point2D;

public class Acceleration {

    // ddx is the second derivative of x(the tempo of the change in velocity
    // ...how fast it changes velocity)
    public double ddx;
    public double ddy;
    public double ddh; //change in heading or rotationalSpeed

    public Acceleration() {
        setAcceleration(0, 0, 0);
    }

    public Acceleration(double ddx, double ddy, double ddh) {
        this();
        setAcceleration(ddx, ddy, ddh);
    }

    public void setAcceleration(double ddx, double ddy, double ddh) {
        this.ddx = ddx;
        this.ddy = ddy;
        this.ddh = ddh;
    }

    public double getMagnitude() {
        return Point2D.distance(0, 0, ddx, ddy);
    }

    //    inertial frame of reference ON the slow moving planet. How fast is the sat coming towards the planet?
    //    As fast as the planet, in the opposite direction as the planet (-dx) + the sat's velocity
    public Acceleration getRelativeAcceleration(Acceleration client) {
        return new Acceleration(-ddx + client.ddx, -ddy + client.ddy, -ddh + client.ddh);
    }

    // returns range [0..Pi] -> [-Pi..0]
    public double getDirection(Acceleration client) {
        return Math.atan2(client.ddx - ddx, client.ddy - ddy);
    }

    public void addToAcceleration(double ddx, double ddy, double ddh) {
        this.ddx += ddx;
        this.ddy += ddy;
        this.ddh += ddh;
    }
}

