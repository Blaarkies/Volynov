package utility

import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Fixture

class RayCastHit(val fixture: Fixture,
                 val point: Vec2,
                 val normal: Vec2,
                 val fraction: Float)
