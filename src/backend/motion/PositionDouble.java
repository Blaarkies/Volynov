package backend.motion;

public class PositionDouble {

    public double x;
    public double y;
    public double heading;

    public PositionDouble() {
        setPosition(0, 0, 0);
    }

    public PositionDouble(double x, double y, double heading) {
        setPosition(x, y, heading);
    }

    public void setPosition(double x, double y, double heading) {
        this.x = x;
        this.y = y;
        this.heading = heading;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void addPosition(double x, double y) {
        this.x += x;
        this.y += y;
    }
}
