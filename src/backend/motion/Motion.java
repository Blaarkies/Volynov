package backend.motion;

public class Motion {

    public PositionDouble position;
    public Velocity velocity;

    // research visitor pattern to use somewhere

    public Motion() {
        this.position = new PositionDouble();
        this.velocity = new Velocity();
    }

    public Motion(double x, double y, double h, double dx, double dy, double dh) {
        this.position = new PositionDouble(x, y, h);
        this.velocity = new Velocity(dx, dy, dh);
    }

    public PositionDouble getPosition() {
        return position;
    }

    public void updatePositionChanges() {
        position.addToPosition(
                velocity.dx,
                velocity.dy,
                velocity.dh
        );
    }
}
