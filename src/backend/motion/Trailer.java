package backend.motion;

public class Trailer {

    // todo: needless class, but trailers are intended to have more properties later on
    public PositionDouble position;

    public Trailer() {
        this.position = new PositionDouble();
    }

    public Trailer(double x, double y) {
        this();
        this.position = new PositionDouble(x, y, 0);
    }
}

