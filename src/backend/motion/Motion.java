package backend.motion;

import java.util.LinkedList;
import java.util.Queue;

public class Motion {

    public PositionDouble position;
    public Velocity velocity;
    public Acceleration acceleration;
    public Queue<Trailer> trailers = new LinkedList<>();
    public int trailerQuantity;

    private Trailer lastTrailer;

    public Motion() {
        this.position = new PositionDouble();
        this.velocity = new Velocity();
        this.acceleration = new Acceleration();
        this.trailerQuantity = 100;
        lastTrailer = new Trailer(0, 0);
        addNewTrailer(this.position.x, this.position.y);
    }

    public Motion(double x, double y, double h, double dx, double dy, double dh, int trailerQuantity) {
        this.position = new PositionDouble(x, y, h);
        this.velocity = new Velocity(dx, dy, dh);
        this.acceleration = new Acceleration();
        this.trailerQuantity = trailerQuantity;
        lastTrailer = new Trailer(x, y);
        addNewTrailer(x, y);
    }

    public void updatePositionChanges() {
        position.addToPosition(
                velocity.dx,
                velocity.dy,
                velocity.dh
        );
        addNewTrailer(position.x, position.y);
    }

    public void updateVelocityChanges() {
        velocity.addToVelocity(
                acceleration.ddx,
                acceleration.ddy,
                acceleration.ddh
        );
    }

    public void addNewTrailer(double x, double y) {
        double distance = position.getDistance(lastTrailer.position);
        if (distance > 5) {
            Trailer nowTrailer = new Trailer(x, y);
            lastTrailer = nowTrailer;

            trailers.add(nowTrailer);
            if (trailers.size() > trailerQuantity) {
                trailers.poll();
            }
        }

    }
}
