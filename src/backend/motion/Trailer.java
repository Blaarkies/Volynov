package backend.motion;

public class Trailer {

    public PositionDouble position;
//    public int age;

    public Trailer() {
        this.position = new PositionDouble();
//        this.age = 0;
    }

    public Trailer(double x, double y) {
        this();
        this.position = new PositionDouble(x, y, 0);
    }
}

