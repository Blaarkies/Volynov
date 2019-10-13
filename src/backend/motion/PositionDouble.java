package backend.motion;

public class PositionDouble {

    public double x;
    public double y;
    public double h; // heading or rotationalPosition

    public PositionDouble() {
        setPosition(0, 0, 0);
    }

    public PositionDouble(double x, double y, double heading) {
        setPosition(x, y, heading);
    }

    public void setPosition(double x, double y, double heading) {
        this.x = x;
        this.y = y;
        this.h = heading;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void addToPosition(double x, double y, double heading) {
        this.x += x;
        this.y += y;
        this.h += heading;
    }

    public double getDistance(PositionDouble positionDouble) { // TODO: Force uses these
        return Math.sqrt(sqr(positionDouble.x - x) + sqr(positionDouble.y - y));
    }

    public double getDirection(PositionDouble client) {
        double theta = Math.atan2(client.y - y, client.x - x);
        theta = (theta < 0)
                ? theta + Math.PI * 2
                : theta;
        return theta;
    }

    public double sqr(double number) {
        return number * number;
    }
}
