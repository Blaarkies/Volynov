package backend.motion;

import java.util.LinkedList;
import java.util.Queue;

public class Motion {

    public PositionDouble position;
    public Velocity velocity;
    public Queue<Trailer> trailers = new LinkedList<>();
    public int trailersPopulation;

    public Motion() {
        this.position = new PositionDouble();
        this.velocity = new Velocity();
        this.trailersPopulation = 100;
        addNewTrailer(this.position.x, this.position.y);
    }

    public Motion(double x, double y, double h, double dx, double dy, double dh, int trailersPopulation) {
        this.position = new PositionDouble(x, y, h);
        this.velocity = new Velocity(dx, dy, dh);
        this.trailersPopulation = trailersPopulation;
        addNewTrailer(x, y);
    }

    public void updatePositionChanges() {
        this.position.addToPosition(
                this.velocity.dx,
                this.velocity.dy,
                this.velocity.dh
        );
        addNewTrailer(this.position.x, this.position.y);
    }

    public void addNewTrailer(double x, double y) {
        this.trailers.add(new Trailer(x, y));
        if (this.trailers.size() > this.trailersPopulation) {
            this.trailers.poll();
        }
    }
}
