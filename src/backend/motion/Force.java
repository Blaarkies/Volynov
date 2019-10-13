package backend.motion;

public class Force {

    public double x;
    public double y;

    public Force() {
        this.x = 0;
        this.y = 0;
    }

    public Force(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getMagnitude() {
        return Math.sqrt(sqr(x) + sqr(y));
    }

    public double sqr(double number) {
        return number * number;
    }

    public double getDirection() { // returns range [0..Pi] -> [-Pi..0]
        double theta = Math.atan2(y, x);
        theta = (theta < 0)
                ? theta + Math.PI * 2
                : theta;
        return theta;
    }

}
