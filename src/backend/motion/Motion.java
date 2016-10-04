package backend.motion;

public class Motion {

    public PositionDouble position;
    public Velocity velocity;

    //visitor pattern

    public Motion() {
        this.velocity = new Velocity();
        this.position = new PositionDouble();
    }

    public Motion(Velocity velocity, PositionDouble postition) {
        this.velocity = velocity;
        this.position = new PositionDouble();
    }

    public PositionDouble getPosition() {
        return position;
    }
}
