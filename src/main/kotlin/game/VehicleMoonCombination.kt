package game

import display.graphic.Color
import engine.freeBody.Planet

data class VehicleMoonCombination(
    val player: GamePlayer,
    val color: Color,
    val moon: Planet,
    val placement: VehiclePlacement
)
