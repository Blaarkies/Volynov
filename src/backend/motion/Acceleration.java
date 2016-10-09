package backend.motion;

import java.awt.geom.Point2D;

public class Acceleration {

    // ddx is the second derivative of x(the tempo of the change in velocity
    // ...how fast does it changes velocity)
    public double ddx;
    public double ddy;
    public double ddh; //change in heading or rotationalSpeed

    public Acceleration() {
        setAcceleration(0,0,0);
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
        return Point2D.distance(0,0, ddx, ddy);
    }

    public double getDirection(Acceleration client) { // returns range [0..Pi] -> [-Pi..0]
        return Math.atan2(client.ddx - this.ddx, client.ddy - this.ddy);
    }

    public void addToAcceleration(double ddx, double ddy, double ddh) {
        this.ddx += ddx;
        this.ddy += ddy;
        this.ddh += ddh;
    }
}

