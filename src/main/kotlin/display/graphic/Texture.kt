package display.graphic

import display.draw.TextureEnum
import org.lwjgl.opengl.ARBFramebufferObject.glGenerateMipmap
import org.lwjgl.opengl.ARBTextureStorage.glTexStorage2D
import org.lwjgl.opengl.ARBTextureStorage.glTexStorage3D
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL30.*
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import utility.Common.getSafePath
import java.nio.ByteBuffer
import kotlin.math.floor
import kotlin.math.pow

class Texture {

    private val id: Int = glGenTextures()

    var width = 0
        set(width) {
            if (width > 0) {
                field = width
            }
        }

    var height = 0
        set(height) {
            if (height > 0) {
                field = height
            }
        }

    constructor()
    constructor(vararg textures: TextureEnum) {
/*
println("""constructor: ${glGetError()}""")

        glEnable(GL_TEXTURE_2D_ARRAY)
        println("""glEnable: ${glGetError()}""")

        val imageBuffers = textures.map { getImageBufferData(it.name) }

//        val mipmapCount = width.times(height).toFloat().pow(.1f).let { floor(it).toInt() }

        bind()
            .setParameter(GL_TEXTURE_WRAP_S, GL_REPEAT)
            .setParameter(GL_TEXTURE_WRAP_T, GL_REPEAT)
            .setParameter(GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
            .setParameter(GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        println("""setParameter: ${glGetError()}""")

        val depth = imageBuffers.size

        glTexStorage3D(GL_TEXTURE_2D_ARRAY, 0, GL_RGBA8, width, height, depth)
//        GL12.glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, GL_RGBA8,
//            width, height, depth,
//            0, GL_RGBA, GL_UNSIGNED_BYTE, buffer)
        println("""glTexStorage3D: ${glGetError()}""")

        unbind()

        imageBuffers.withIndex().forEach { (index, data) ->
            glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, index, width, height, 1, GL_RGBA, GL_UNSIGNED_BYTE, data)
        }

//        glGenerateMipmap(GL_TEXTURE_2D_ARRAY)
        println("""glGetError(): ${glGetError()}""")
        unbind()
        */
    }

    fun getImageBufferData(name: String): ImageByteBuffer {
        val resourcePath = "./textures/$name.png"
        val safePath = getSafePath(resourcePath)

        MemoryStack.stackPush().use { stack ->
            val w = stack.mallocInt(1)
            val h = stack.mallocInt(1)
            val channelCount = stack.mallocInt(1)

            STBImage.stbi_set_flip_vertically_on_load(true)
            val imageByteBuffer = STBImage.stbi_load(safePath, w, h, channelCount, 4)
                ?: throw RuntimeException(
                    """
                    |Cannot load texture file at [$safePath] 
                    |${STBImage.stbi_failure_reason()}""".trimMargin()
                )
            val imageChannels = channelCount.get()
            if (imageChannels != 4) {
                throw Exception(
                    "Texture [$name] tried to load a $imageChannels-channel image format. " +
                            "Only 4-channel, 32-bit PNG images are supported"
                )
            }

//            width = w.get()
//            height = h.get()

            return ImageByteBuffer(imageByteBuffer, w.get(), h.get())
        }
    }

    fun bind(): Texture {
        glBindTexture(GL_TEXTURE_2D_ARRAY, id)
        return this
    }

    fun unbind(): Texture {
        glBindTexture(GL_TEXTURE_2D_ARRAY, 0)
        return this
    }

    fun setParameter(name: Int, value: Int): Texture {
        glTexParameteri(GL_TEXTURE_2D_ARRAY, name, value)
        return this
    }

    fun uploadData(width: Int, height: Int, data: ByteBuffer, mipmapLevels: Int): Texture {
        glTexStorage2D(GL_TEXTURE_2D, mipmapLevels, GL_RGBA8, width, height)
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, data)
        glGenerateMipmap(GL_TEXTURE_2D)
        return this
    }

    fun delete() {
        glDeleteTextures(id)
    }

    companion object {

        fun oldCreateTexture(width: Int, height: Int, data: ByteBuffer): Texture {
            val texture = Texture()
            texture.width = width
            texture.height = height
            val mipmapCount = width.times(height).toFloat().pow(.1f).let { floor(it).toInt() }

//            texture.bind()
//                .setParameter(GL_TEXTURE_WRAP_S, GL_REPEAT)
//                .setParameter(GL_TEXTURE_WRAP_T, GL_REPEAT)
//                .setParameter(GL_TEXTURE_MAG_FILTER, GL_LINEAR)
//                .setParameter(GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
//                .uploadData(width, height, data, mipmapCount)

            return texture
        }

        fun getImageBufferData(name: String): ImageByteBuffer {
            return Texture().getImageBufferData(name)
        }

    }

}

class ImageByteBuffer(val buffer: ByteBuffer,
                      val width: Int,
                      val height: Int) {

}
