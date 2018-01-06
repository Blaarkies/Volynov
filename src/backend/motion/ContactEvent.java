package backend.motion;

import backend.FreeBody;
import com.sun.javafx.geom.Vec2d;

public class ContactEvent {

    public Force normalForce;
    public FreeBody server;

    public ContactEvent(Vec2d normalForce, FreeBody server) {
        this.normalForce = new Force(normalForce.x, normalForce.y);
        this.server = server;
    }

    public ContactEvent(Force normalForce, FreeBody server) {
        this.normalForce = normalForce;
        this.server = server;
    }
}
