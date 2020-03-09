package engine.physics

import engine.freeBody.FreeBody

class GravityCell(var totalMass: Float = 0f, val freeBodies: MutableList<FreeBody> = mutableListOf())

