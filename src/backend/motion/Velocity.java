package backend.motion;

import java.awt.geom.Point2D;

public class Velocity {

    public double dx; //derivative/change in x
    public double dy;
    public double dh; //rotationalSpeed

    public Velocity() {
        this.dx = 0;
        this.dy = 0;
        this.dh = 0;
//        setVelocty(0, 0, 0);
    }

    public Velocity(double dx, double dy, double dh) {
        this.dx = dx;
        this.dy = dy;
        this.dh = dh;
//        setVelocty(dx, dy, dh);
    }

    public void setVelocty(double dx, double dy, double dh) {
        this.dx = dx;
        this.dy = dy;
        this.dh = dh;
    }

    public double getMagnitude() {
        return Point2D.distance(0,0, dx, dy);
    }

    public double getDirection() { // returns -180 -> 180 range
//        dot = x1*x2 + y1*y2      # dot product
//        det = x1*y2 - y1*x2      # determinant
//        angle = atan2(det, dot)  # atan2(dy, dx) or atan2(sin, cos)
        double dot = dx *1 + dy *0;
        double det = dx *0 - dy *1;
        double angle = Math.atan2(det, dot);
        return angle;
    }

    public void addToVelocity(double dx, double dy, double dh) {
        this.dx += dx;
        this.dy += dy;
        this.dh += dh;
    }
}

