package engine.freeBody

import display.draw.TextureConfig
import org.jbox2d.dynamics.Body

class Particle(
    val id: String,
    val worldBody: Body,
    var radius: Float,
    val textureConfig: TextureConfig
) {

    var fullRadius: Float = radius

    private val currentTime
        get() = System.currentTimeMillis()

    val ageTime
        get() = (currentTime - createdAt)

    private val createdAt = currentTime

}
