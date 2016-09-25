package backend.motion;

import java.awt.geom.Point2D;

public class Velocity {

    public double x;
    public double y;
    public double rotationalSpeed;

    public Velocity() {
        setVelocty(0, 0, 0);
    }

    public Velocity(double x, double y, double rotationalSpeed) {
        setVelocty(x, y, rotationalSpeed);
    }

    public void setVelocty(double x, double y, double rotationalSpeed) {
        this.x = x;
        this.y = y;
        this.rotationalSpeed = rotationalSpeed;
    }

    public double getMagnitude() {
        return Point2D.distance(0,0,x,y);
    }

    public double getDirection() { // returns -180 -> 180 range
//        dot = x1*x2 + y1*y2      # dot product
//        det = x1*y2 - y1*x2      # determinant
//        angle = atan2(det, dot)  # atan2(y, x) or atan2(sin, cos)
        double dot = x*1 + y*0;
        double det = x*0 - y*1;
        double angle = Math.atan2(det, dot);
        return angle;
    }
}