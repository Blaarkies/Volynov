package display.graphic

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import org.lwjgl.stb.STBImage.*
import org.lwjgl.system.MemoryStack
import utility.Common.getSafePath
import java.io.File
import java.nio.ByteBuffer

class Texture {

    private val id = glGenTextures()

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

    fun bind(): Texture {
        glBindTexture(GL_TEXTURE_2D, id)
        return this
    }

    fun setParameter(name: Int, value: Int): Texture {
        glTexParameteri(GL_TEXTURE_2D, name, value)
        return this
    }

    fun uploadData(width: Int, height: Int, data: ByteBuffer) {
        uploadData(GL_RGBA8, width, height, GL_RGBA, data)
    }

    fun uploadData(internalFormat: Int, width: Int, height: Int, format: Int, data: ByteBuffer): Texture {
        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, data)
        return this
    }

    fun delete() {
        glDeleteTextures(id)
    }

    companion object {

        fun createTexture(width: Int, height: Int, data: ByteBuffer): Texture {
            val texture = Texture()
            texture.width = width
            texture.height = height
            texture.bind()
                .setParameter(GL_TEXTURE_WRAP_S, GL_REPEAT)
                .setParameter(GL_TEXTURE_WRAP_T, GL_REPEAT)
                .setParameter(GL_TEXTURE_MIN_FILTER, GL_LINEAR)
                .setParameter(GL_TEXTURE_MAG_FILTER, GL_LINEAR)
                .uploadData(GL_RGBA8, width, height, GL_RGBA, data)
            return texture
        }

        fun loadTexture(resourcePath: String): Texture {
            val safePath = getSafePath(resourcePath)

            var image = BufferUtils.createByteBuffer(0)
            var width = 0
            var height = 0
            MemoryStack.stackPush().use { stack ->
                val w = stack.mallocInt(1)
                val h = stack.mallocInt(1)
                val comp = stack.mallocInt(1)

                stbi_set_flip_vertically_on_load(true)
                image = stbi_load(safePath, w, h, comp, 0)
                    ?: throw RuntimeException("Cannot load texture file at $safePath \n${stbi_failure_reason()}")

                width = w.get()
                height = h.get()
            }
            return createTexture(width, height, image)
        }

    }

}
