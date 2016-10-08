package backend.motion;

import java.awt.geom.Point2D;

public class Velocity {

    // dx is the derivative of x(the tempo of the change in position...how fast does it move)
    public double dx;
    public double dy;
    public double dh; //change in heading or rotationalSpeed

    public Velocity() {
        setVelocty(0,0,0);
    }

    public Velocity(double dx, double dy, double dh) {
        this();
        setVelocty(dx, dy, dh);
    }

    public void setVelocty(double dx, double dy, double dh) {
        this.dx = dx;
        this.dy = dy;
        this.dh = dh;
    }

    public double getMagnitude() {
        return Point2D.distance(0,0, dx, dy);
    }

    public double getDirection(Velocity client) { // returns range [0..Pi] -> [-Pi..0]
        return Math.atan2(client.dx - this.dx, client.dy - this.dy);
    }

    public void addToVelocity(double dx, double dy, double dh) {
        this.dx += dx;
        this.dy += dy;
        this.dh += dh;
    }
}

