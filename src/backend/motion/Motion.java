package backend.motion;

import java.util.ArrayList;
import java.util.List;

public class Motion {

    public PositionDouble position;
    public Velocity velocity;
    public List<Trailer> trailers = new ArrayList<>();

    // research visitor pattern to use somewhere

    public Motion() {
        this.position = new PositionDouble();
        this.velocity = new Velocity();
        addNewTrailer(this.position.x, this.position.y);
    }

    public Motion(double x, double y, double h, double dx, double dy, double dh) {
        this.position = new PositionDouble(x, y, h);
        this.velocity = new Velocity(dx, dy, dh);
        addNewTrailer(x, y);
    }

    public PositionDouble getPosition() {
        return this.position;
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
        if (this.trailers.size() > 500) {
            this.trailers.remove(0);
        }
    }
}
