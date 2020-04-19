package display.draw

import display.graphic.Texture

class TextureHolder {

    private val textureHashMap = HashMap<TextureEnum, Texture>()

    fun init() {
        val hMap = textureHashMap
        hMap[TextureEnum.marble_earth] = Texture.loadTexture("./textures/marble_earth.png")
        hMap[TextureEnum.full_moon] = Texture.loadTexture("./textures/full_moon.png")
        hMap[TextureEnum.metal] = Texture.loadTexture("./textures/metal.png")
        hMap[TextureEnum.pavement] = Texture.loadTexture("./textures/pavement.png")
        hMap[TextureEnum.white_pixel] = Texture.loadTexture("./textures/white_pixel.png")
        hMap[TextureEnum.stars_2k] = Texture.loadTexture("./textures/stars_2k.png")
        hMap[TextureEnum.icon_aim] = Texture.loadTexture("./textures/icon_aim.png")
    }

    fun getTexture(texture: TextureEnum): Texture = textureHashMap[texture].let {
        checkNotNull(it) { "Cannot find texture $texture" }
        it
    }

}
