package game

import engine.freeBody.FreeBody
import org.jbox2d.common.Vec2

class TrajectoryPrediction(val timeStamp: Long,
                           val warheadPath: List<Vec2> = listOf(),
                           val totalDistance: Float = 0f,
                           val nearbyFreeBodies: List<FreeBody> = listOf())
