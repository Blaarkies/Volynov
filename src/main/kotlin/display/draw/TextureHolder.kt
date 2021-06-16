package display.draw

import display.graphic.Texture

class TextureHolder {

    private val hashMap = HashMap<TextureEnum, Texture>()

    fun init() {
        TextureEnum.values()
            .forEach { hashMap[it] = Texture(it) }
    }

    fun getTexture(texture: TextureEnum): Texture = hashMap[texture].let {
        checkNotNull(it) { "Cannot find texture $texture" }
        it
    }

}
