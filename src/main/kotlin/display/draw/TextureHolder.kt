package display.draw

import display.graphic.Texture

class TextureHolder {

    private val textureHashMap = HashMap<TextureEnum, Texture>()

    fun init() {
        val hMap = textureHashMap
        hMap[TextureEnum.marble_earth] = Texture.loadTexture("src\\main\\resources\\textures\\marble_earth.png")
        hMap[TextureEnum.full_moon] = Texture.loadTexture("src\\main\\resources\\textures\\full_moon.png")
        hMap[TextureEnum.metal] = Texture.loadTexture("src\\main\\resources\\textures\\metal.png")
        hMap[TextureEnum.pavement] = Texture.loadTexture("src\\main\\resources\\textures\\pavement.png")
        hMap[TextureEnum.white_pixel] = Texture.loadTexture("src\\main\\resources\\textures\\white_pixel.png")
        hMap[TextureEnum.stars_2k] = Texture.loadTexture("src\\main\\resources\\textures\\stars_2k.png")
    }

    fun getTexture(texture: TextureEnum): Texture = textureHashMap[texture].let {
        checkNotNull(it) { "Cannot find texture $texture" }
        it
    }

}
