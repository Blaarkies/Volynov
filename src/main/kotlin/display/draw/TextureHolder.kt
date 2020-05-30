package display.draw

import display.graphic.Texture

class TextureHolder {

    private val textureHashMap = HashMap<TextureEnum, Texture>()

    fun init() {
        TextureEnum.values()
            .forEach { textureHashMap[it] = Texture.loadTexture("./textures/${it.name}.png") }
    }

    fun getTexture(texture: TextureEnum): Texture = textureHashMap[texture].let {
        checkNotNull(it) { "Cannot find texture $texture" }
        it
    }

}
