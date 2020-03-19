package display.text

import display.graphic.Color
import display.graphic.Renderer
import display.graphic.Texture
import org.jbox2d.common.Vec2
import org.lwjgl.system.MemoryUtil
import java.awt.Font
import java.awt.Font.*
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.InputStream
import kotlin.math.hypot
import java.awt.Color as AwtColor


class Font constructor(font: Font = Font(MONOSPACED, BOLD, 32), antiAlias: Boolean = true) {

    private val glyphs: MutableMap<Char, Glyph>
    private var fontBitMapShadow: Texture = Texture()
    private val fontBitMap: Texture
    private var fontHeight = 0

    constructor(antiAlias: Boolean) : this(Font(MONOSPACED, PLAIN, 16), antiAlias)

    constructor(size: Int) : this(Font(MONOSPACED, PLAIN, size), true)

    constructor(size: Int, antiAlias: Boolean) : this(Font(MONOSPACED, PLAIN, size), antiAlias)

    constructor(inputStream: InputStream, size: Int) : this(inputStream, size, true)

    constructor(inputStream: InputStream, size: Int, antiAlias: Boolean) : this(
        createFont(TRUETYPE_FONT, inputStream).deriveFont(
            PLAIN,
            size.toFloat()
        ), antiAlias
    )

    private fun createFontTexture(font: Font, antiAlias: Boolean): List<Texture> {
        var imageWidth = 0
        var imageHeight = 0

        (32..255).filter { it != 127 }
            .mapNotNull { createCharImage(font, it.toChar(), antiAlias) }
            .forEach {
                imageWidth += it.width
                imageHeight = imageHeight.coerceAtLeast(it.height)
            }
        fontHeight = imageHeight

        var image = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()
        var x = 0

        (32..255).filter { it != 127 }
            .forEach {
                val c = it.toChar()
                val charImage = createCharImage(font, c, antiAlias) ?: return@forEach
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

        val shadowTexture = createShadowTexture(image)

        val width = image.width
        val height = image.height
        val pixels = IntArray(width * height)
        image.getRGB(0, 0, width, height, pixels, 0, width)
        val buffer = MemoryUtil.memAlloc(width * height * 4)

        val bytePixels = pixels.flatMap {
            listOf(
                (it shr 16 and 0xFF).toByte(),/* Red component 0xAARRGGBB >> 16 = 0x0000AARR */
                (it shr 8 and 0xFF).toByte(),/* Green component 0xAARRGGBB >> 8 = 0x00AARRGG */
                (it and 0xFF).toByte(),/* Blue component 0xAARRGGBB >> 0 = 0xAARRGGBB */
                (it shr 24 and 0xFF).toByte()/* Alpha component 0xAARRGGBB >> 24 = 0x000000AA */
            )
        }.toByteArray()
        buffer.put(bytePixels)
        buffer.flip()

        val fontTexture = Texture.createTexture(width, height, buffer)
        MemoryUtil.memFree(buffer)
        return listOf(fontTexture, shadowTexture)
    }

    private fun createShadowTexture(image: BufferedImage): Texture {
        val width = image.width
        val height = image.height
        val pixels = IntArray(width * height)
        image.getRGB(0, 0, width, height, pixels, 0, width)

        val imageLines = pixels.toList()
            .chunked(width)
            .map { it.map { pixel -> pixel shr 24 and 0xFF } }

        val radius = 2
        val kernel = (-radius until +radius).flatMap { i -> (-radius until +radius).map { arrayOf(it, i) } }
            .filter { (i, j) -> hypot(i.toFloat(), j.toFloat()) <= radius }

        val dilatedImage = (0 until height).flatMap { i -> (0 until width).map { arrayOf(it, i) } }
            .map { (i, j) ->

                val isInk = kernel.filter { (k, l) ->
                    (i + k) > 0
                            && (i + k) < width
                            && (j + l) > 0
                            && (j + l) < height
                }.any { (k, l) -> imageLines[j + l][i + k] > 0 }
                if (isInk) 255 else 0
            }
        val buffer = MemoryUtil.memAlloc(width * height * 4)
        val bytePixels = dilatedImage.flatMap {
            listOf(
                255.toByte(),/* Red component 0xAARRGGBB >> 16 = 0x0000AARR */
                255.toByte(),/* Green component 0xAARRGGBB >> 8 = 0x00AARRGG */
                255.toByte(),/* Blue component 0xAARRGGBB >> 0 = 0xAARRGGBB */
                it.toByte()/* Alpha component 0xAARRGGBB >> 24 = 0x000000AA */
            )
        }.toByteArray()
        buffer.put(bytePixels)
        buffer.flip()

        val shadowTexture = Texture.createTexture(width, height, buffer)
        MemoryUtil.memFree(buffer)
        return shadowTexture
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

    private fun getHeight(text: CharSequence): Int {
        var height = 0
        var lineHeight = 0
        val a =text.split('\n')
            .map { sentence -> sentence.maxBy { letter -> glyphs[letter]!!.height } }

//            .forEach {
//            if (it == '\n') {
//                height += lineHeight
//                lineHeight = 0
//                return@forEach
//            }
//            if (it == '\r' || it.isWhitespace()) {
//                return@forEach
//            }
//            val g = glyphs[it]
//            lineHeight = lineHeight.coerceAtLeast(g!!.height)
//        }

        return height + lineHeight
    }

    fun drawText(renderer: Renderer, text: CharSequence, offset: Vec2, scale: Vec2, c: Color) {
        val textHeight = getHeight(text)
        drawLetters(fontBitMapShadow, offset, scale, textHeight, renderer, text, Color.BLACK)
        drawLetters(fontBitMap, offset, scale, textHeight, renderer, text, c)
    }

    private fun drawLetters(
        texture: Texture,
        offset: Vec2, scale: Vec2,
        textHeight: Int,
        renderer: Renderer,
        text: CharSequence,
        c: Color
    ) {
        var drawX = offset.x
        var drawY = offset.y
        if (textHeight > fontHeight) {
            drawY += textHeight - fontHeight.toFloat()
        }

        renderer.begin()
        text.forEach {
            if (it == '\n') {
                drawY -= fontHeight.toFloat()
                drawX = offset.x
                return@forEach
            }
            if (it == '\r' || it.isWhitespace()) {
                return@forEach
            }
            val glyph = glyphs[it]!!
            drawTextPosition(texture, renderer, drawX, drawY, scale, glyph, c)
            drawX += glyph.width.toFloat()
        }
        renderer.end()
    }

    private fun drawTextPosition(
        texture: Texture,
        renderer: Renderer,
        drawX: Float,
        drawY: Float,
        scale: Vec2,
        glyph: Glyph,
        c: Color
    ) {
        texture.bind()
        renderer.drawTextureRegion(
            texture,
            drawX - glyph.width.toFloat() * 0.5f,
            drawY - glyph.height.toFloat() * 0.5f,
            glyph.x.toFloat(),
            glyph.y.toFloat(),
            glyph.width.toFloat(),
            glyph.height.toFloat(),
            c
        )
    }

    fun dispose() {
        fontBitMap.delete()
    }

    init {
        glyphs = HashMap()
        val (texture, shadowTexture) = createFontTexture(font, antiAlias)
        fontBitMap = texture
        fontBitMapShadow = shadowTexture
    }

    companion object {

        fun deepCopy(bufferedImage: BufferedImage): BufferedImage {
            val raster = bufferedImage.copyData(null)
            return BufferedImage(
                bufferedImage.colorModel,
                raster,
                bufferedImage.colorModel.isAlphaPremultiplied, null
            )
        }
    }
}
