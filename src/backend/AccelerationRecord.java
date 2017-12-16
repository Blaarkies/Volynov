package backend;

import backend.motion.Acceleration;
import backend.motion.Motion;

public class AccelerationRecord {

    public Motion motion;
    public Acceleration accelerationToAdd;

    public AccelerationRecord(Motion motion, Acceleration accelerationToAdd) {
        this.motion = motion;
        this.accelerationToAdd = accelerationToAdd;
    }

}
