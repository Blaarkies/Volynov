package backend.motion;

import java.util.LinkedList;
import java.util.Queue;

public class Motion {

    public PositionDouble position;
    public Velocity velocity;
    public Acceleration acceleration;
    public Queue<Trailer> trailers = new LinkedList<>();
    public int trailersPopulation;

    public Motion() {
        this.position = new PositionDouble();
        this.velocity = new Velocity();
        this.acceleration = new Acceleration();
        this.trailersPopulation = 100;
        addNewTrailer(this.position.x, this.position.y);
    }

    public Motion(double x, double y, double h, double dx, double dy, double dh, int trailersPopulation) {
        this.position = new PositionDouble(x, y, h);
        this.velocity = new Velocity(dx, dy, dh);
        this.acceleration = new Acceleration();
        this.trailersPopulation = trailersPopulation;
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
        trailers.add(new Trailer(x, y));
        if (trailers.size() > trailersPopulation) {
            trailers.poll();
        }
    }
}
