package engine.gameState

import engine.freeBody.Vehicle
import engine.freeBody.Warhead
import game.TrajectoryPrediction
import org.jbox2d.common.Vec2

object GameStateSimulator {

    fun getNewPrediction(maxDistance: Float,
                         accuracy: Float,
                         parentGameState: GameState,
                         parentVelocityIterations: Int,
                         parentPositionIterations: Int,
                         parentTimeStep: Float,
                         lastPrediction: TrajectoryPrediction): TrajectoryPrediction {
        val timeStamp = System.currentTimeMillis()
        val clonedState = parentGameState.clone()
        val player = clonedState.playerOnTurn!!
        player.vehicle?.fireWarhead(clonedState, player, "") {}

        val errorScale = 1f / accuracy
        val predictionVelocityIterations = parentVelocityIterations.times(accuracy).toInt().coerceAtLeast(1)
        val predictionPositionIterations = parentPositionIterations.times(accuracy).toInt().coerceAtLeast(1)
        clonedState.tickClock(parentTimeStep * errorScale, predictionVelocityIterations,
            predictionPositionIterations)
        val predictionWarhead = player.warheads.last()
        var totalDistance = 0f

        return sequence<Vec2> {
            var lastLocation: Vec2? = null

            repeat(400.times(accuracy).toInt().coerceAtLeast(1)) {
                if (totalDistance > maxDistance
                    || clonedState.warheads.isEmpty()
                    || clonedState.warheads.last() != predictionWarhead
                    || timeStamp < lastPrediction.timeStamp) {
                    return@sequence
                }
                val location = predictionWarhead.worldBody.position.clone()
                yield(location)

                totalDistance += lastLocation?.sub(location)?.length() ?: 0f
                lastLocation = location
                clonedState.tickClock(parentTimeStep * errorScale, predictionVelocityIterations,
                    predictionPositionIterations)
            }
        }.windowed(1, 4.times(accuracy).toInt().coerceAtLeast(1)).flatten()
            .toList().let {
                val lastLocation = it.last()
                val nearbyFreeBodies = clonedState.gravityBodies
                    .filter { body ->
                        body !is Vehicle
                                && body !is Warhead
                                && body.worldBody.position.sub(lastLocation).length()
                            .minus(body.radius) < 7f
                    }
                TrajectoryPrediction(timeStamp, it, totalDistance, nearbyFreeBodies)
            }
    }

}
