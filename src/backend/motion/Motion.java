package backend.motion;

public class Motion {

    public PositionDouble postition;
    public Velocity velocity;

    public Motion() {
        this.velocity = new Velocity();
        this.postition = new PositionDouble();
    }

    public Motion(Velocity velocity, PositionDouble postition) {
        this.velocity = velocity;
        this.postition = new PositionDouble();
    }
}
