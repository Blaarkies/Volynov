package display.draw

import display.graphic.Texture

class TextureHolder {

    lateinit var marble_earth: Texture
    lateinit var full_moon: Texture
    lateinit var metal: Texture
    lateinit var pavement: Texture
    lateinit var white_pixel: Texture

    fun init() {
        marble_earth = Texture.loadTexture("src\\main\\resources\\textures\\marble_earth.png")
        full_moon = Texture.loadTexture("src\\main\\resources\\textures\\full_moon.png")
        metal = Texture.loadTexture("src\\main\\resources\\textures\\metal.png")
        pavement = Texture.loadTexture("src\\main\\resources\\textures\\pavement.png")
        white_pixel = Texture.loadTexture("src\\main\\resources\\textures\\white_pixel.png")
    }

}
