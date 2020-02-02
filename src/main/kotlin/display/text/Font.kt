package display.text

import display.graphic.Color
import display.graphic.Renderer
import display.graphic.Texture
import org.lwjgl.system.MemoryUtil
import java.awt.Font
import java.awt.Font.*
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.InputStream
import java.awt.Color as AwtColor

class Font constructor(font: Font = Font(MONOSPACED, BOLD, 32), antiAlias: Boolean = true) {

    private val glyphs: MutableMap<Char, Glyph>
    private val texture: Texture
    private var fontHeight = 0

    constructor(antiAlias: Boolean) : this(Font(MONOSPACED, PLAIN, 16), antiAlias)

    constructor(size: Int) : this(Font(MONOSPACED, PLAIN, size), true)

    constructor(size: Int, antiAlias: Boolean) : this(Font(MONOSPACED, PLAIN, size), antiAlias)

    constructor(`in`: InputStream?, size: Int) : this(`in`, size, true)

    constructor(`in`: InputStream?, size: Int, antiAlias: Boolean) : this(
        createFont(TRUETYPE_FONT, `in`).deriveFont(
            PLAIN,
            size.toFloat()
        ), antiAlias
    )

    private fun createFontTexture(font: Font, antiAlias: Boolean): Texture {
        var imageWidth = 0
        var imageHeight = 0

        for (i in 32..255) {
            if (i == 127) {
                continue
            }
            val c = i.toChar()
            val ch = createCharImage(font, c, antiAlias)
                ?: continue
            imageWidth += ch.width
            imageHeight = imageHeight.coerceAtLeast(ch.height)
        }
        fontHeight = imageHeight

        var image = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()
        var x = 0

        for (i in 32..255) {
            if (i == 127) {
                continue
            }
            val c = i.toChar()
            val charImage = createCharImage(font, c, antiAlias) ?: continue
            val charWidth = charImage.width
            val charHeight = charImage.height

            val ch = Glyph(charWidth, charHeight, x, image.height - charHeight, 0f)
            g.drawImage(charImage, x, 0, null)
            x += ch.width
            glyphs[c] = ch
        }

        val transform = AffineTransform.getScaleInstance(1.0, -1.0)
        transform.translate(.0, -image.height.toDouble())
        val operation = AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR)

        val outImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)
        operation.filter(image, outImage)
        image = outImage

        val width = image.width
        val height = image.height
        val pixels = IntArray(width * height)
        image.getRGB(0, 0, width, height, pixels, 0, width)

        val buffer = MemoryUtil.memAlloc(width * height * 4)
        for (i in 0 until height) {
            for (j in 0 until width) {
                /* Pixel as RGBA: 0xAARRGGBB */
                val pixel = pixels[i * width + j]
                /* Red component 0xAARRGGBB >> 16 = 0x0000AARR */
                buffer.put((pixel shr 16 and 0xFF).toByte())
                /* Green component 0xAARRGGBB >> 8 = 0x00AARRGG */
                buffer.put((pixel shr 8 and 0xFF).toByte())
                /* Blue component 0xAARRGGBB >> 0 = 0xAARRGGBB */
                buffer.put((pixel and 0xFF).toByte())
                /* Alpha component 0xAARRGGBB >> 24 = 0x000000AA */
                buffer.put((pixel shr 24 and 0xFF).toByte())
            }
        }
        buffer.flip()

        val fontTexture = Texture.createTexture(width, height, buffer)
        MemoryUtil.memFree(buffer)
        return fontTexture
    }

    private fun createCharImage(font: Font, c: Char, antiAlias: Boolean): BufferedImage? {
        var image = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
        var g = image.createGraphics()
        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        }
        g.font = font
        val metrics = g.fontMetrics
        g.dispose()

        val charWidth = metrics.charWidth(c)
        val charHeight = metrics.height
        if (charWidth == 0) {
            return null
        }
        image = BufferedImage(charWidth, charHeight, BufferedImage.TYPE_INT_ARGB)
        g = image.createGraphics()
        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        }
        g.font = font
        g.paint = AwtColor.WHITE
        g.drawString(c.toString(), 0, metrics.ascent)
        g.dispose()
        return image
    }

    fun getWidth(text: CharSequence): Int {
        var width = 0
        var lineWidth = 0
        for (element in text) {
            val c = element
            if (c == '\n') {
                width = width.coerceAtLeast(lineWidth)
                lineWidth = 0
                continue
            }
            if (c == '\r') {
                continue
            }
            val g = glyphs[c]
            lineWidth += g!!.width
        }
        width = width.coerceAtLeast(lineWidth)
        return width
    }

    fun getHeight(text: CharSequence): Int {
        var height = 0
        var lineHeight = 0
        for (element in text) {
            if (element == '\n') {
                height += lineHeight
                lineHeight = 0
                continue
            }
            if (element == '\r' || element.isWhitespace()) {
                continue
            }
            val g = glyphs[element]
            lineHeight = lineHeight.coerceAtLeast(g!!.height)
        }
        height += lineHeight
        return height
    }

    fun drawText(renderer: Renderer, text: CharSequence, x: Float, y: Float, c: Color) {
        val textHeight = getHeight(text)
        var drawX = x
        var drawY = y
        if (textHeight > fontHeight) {
            drawY += textHeight - fontHeight.toFloat()
        }
        texture.bind()
        renderer.begin()
        for (element in text) {
            if (element == '\n') {
                drawY -= fontHeight.toFloat()
                drawX = x
                continue
            }
            if (element == '\r' || element.isWhitespace()) {
                continue
            }
            val g = glyphs[element]!!
            renderer.drawTextureRegion(
                texture,
                drawX - g.width.toFloat() * 0.5f + 2f,
                drawY - g.height.toFloat() * 0.5f + 2f,
                g.x.toFloat(),
                g.y.toFloat(),
                g.width.toFloat(),
                g.height.toFloat(),
                Color.BLACK
            )

            renderer.drawTextureRegion(
                texture,
                drawX - g.width.toFloat() * 0.5f,
                drawY - g.height.toFloat() * 0.5f,
                g.x.toFloat(),
                g.y.toFloat(),
                g.width.toFloat(),
                g.height.toFloat(),
                c
            )
            drawX += g.width.toFloat()
        }
        renderer.end()
    }

    fun drawText(renderer: Renderer, text: CharSequence, x: Float, y: Float) {
        drawText(renderer, text, x, y, Color.WHITE)
    }

    fun dispose() {
        texture.delete()
    }

    init {
        glyphs = HashMap()
        texture = createFontTexture(font, antiAlias)
    }
}
