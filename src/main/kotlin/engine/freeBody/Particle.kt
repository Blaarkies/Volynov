package engine.freeBody

import display.draw.TextureConfig
import org.jbox2d.dynamics.Body

class Particle(
    val id: String,
    val worldBody: Body,
    val radius: Float,
    val textureConfig: TextureConfig
) {

    private val currentTime
        get() = System.currentTimeMillis()

    val ageTime
        get() = (currentTime - createdAt)

    private val createdAt = currentTime

}
