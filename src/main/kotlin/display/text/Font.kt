package display.text

import display.graphic.*
import display.graphic.vertex.BasicShapes
import org.jbox2d.common.Vec2
import org.lwjgl.system.MemoryUtil
import java.awt.Font
import java.awt.Font.*
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.InputStream
import java.lang.NullPointerException
import kotlin.math.hypot
import java.awt.Color as AwtColor

class Font(font: Font = Font(MONOSPACED, BOLD, 32), antiAlias: Boolean = true) {

    private val glyphs: MutableMap<Char, Glyph> = HashMap()
    private var fontBitMapShadow: Texture = Texture()
    private val fontBitMap: Texture = Texture()
    private var fontHeight = 0
    private var fontWidth = 0

    constructor(inputStream: InputStream, size: Int) : this(inputStream, size, true)

    constructor(inputStream: InputStream, size: Int, antiAlias: Boolean) : this(
        createFont(TRUETYPE_FONT, inputStream).deriveFont(
            PLAIN,
            size.toFloat()
        ), antiAlias
    )

    init {
//        glyphs = HashMap()
//        val (texture, shadowTexture) = createFontTexture(font, antiAlias)
//        fontBitMap = texture
//        fontBitMapShadow = shadowTexture
//
//        fontWidth = glyphs.map { it.value.width }.maxOrNull()!!
    }

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

                val ch = Glyph(charWidth, charHeight, x, image.height - charHeight)
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

        val fontTexture = Texture.oldCreateTexture(width, height, buffer)
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

        val shadowTexture = Texture.oldCreateTexture(width, height, buffer)
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

    private fun getHeight(text: CharSequence): Int = text.split('\n')
        .map { sentence -> sentence.map { letter -> glyphs[letter]!!.height }.maxOrNull() ?: 0 }
        .sumBy { it }

    private fun getTextTotalWidth(glyphs: List<Glyph>, scale: Vec2) = glyphs.map { it.width * scale.x }.sum()

    fun drawText(
        oldRenderer: OldRenderer,
        text: CharSequence,
        offset: Vec2,
        scale: Vec2,
        color: Color,
        justify: TextJustify,
        useCamera: Boolean,
        snipRegion: SnipRegion?,
        maxWidth: Float,
        angle: Float
    ) {
        drawLetters(fontBitMapShadow, offset, scale, oldRenderer, text, Color.BLACK, justify, useCamera, snipRegion,
            maxWidth, angle)
        drawLetters(fontBitMap, offset, scale, oldRenderer, text, color, justify, useCamera, snipRegion, maxWidth, angle)
    }

    private fun drawLetters(
        texture: Texture,
        offset: Vec2,
        scale: Vec2,
        oldRenderer: OldRenderer,
        text: CharSequence,
        color: Color,
        justify: TextJustify,
        useCamera: Boolean,
        snipRegion: SnipRegion?,
        maxWidth: Float,
        angle: Float
    ) {
        val glyphsList = text.mapNotNull {
            try {
                glyphs[it]
            } catch (e: NullPointerException) {
                throw Exception("Could not find text character [$it] in font", e)
            }
        }

        val spaceIndexes = glyphsList.withIndex()
            .filter { (_, g) -> g == glyphs[32.toChar()] } // 32 = space char
            .map { (index, _) -> index }
        var totalLength = 0f
        val lineLengthAtChars = glyphsList.withIndex()
            .map { (index, g) ->
                totalLength += g.width * scale.x
                Pair(index, totalLength)
            }

        var lastEndLine = 0f
        val endLineIndexes = mutableMapOf<Int, Boolean>()
        if (maxWidth > 0f) {
            lineLengthAtChars.forEach { (charIndex, length) ->
                if (length - lastEndLine > maxWidth) {
                    val lastSpaceIndex = spaceIndexes.reversed().find { spaceIndex -> spaceIndex < charIndex }
                    endLineIndexes[lastSpaceIndex!!] = true
                    lastEndLine = lineLengthAtChars[lastSpaceIndex].second
                }
            }
        }

        val justifyment = when (justify) {
            TextJustify.LEFT -> Vec2()
            TextJustify.CENTER -> Vec2(-getTextTotalWidth(glyphsList, scale), 0f)
            TextJustify.RIGHT -> Vec2(-getTextTotalWidth(glyphsList, scale) * 2, 0f)
        }

        val lineHeight = glyphsList.maxByOrNull { it.height }!!.height * 2
        var x = 0f
        var y = 0f
        glyphsList.withIndex().forEach { (index, it) ->
            x += it.width * scale.x

            drawTextPosition(texture, oldRenderer, Vec2(x, y).add(offset).add(justifyment),
                scale, it, color, useCamera, snipRegion, angle)
            x += it.width * scale.x

            if (endLineIndexes[index] == true) {
                x = 0f
                y -= lineHeight * scale.y
            }
        }
    }

    private fun drawTextPosition(
        texture: Texture,
        oldRenderer: OldRenderer,
        offset: Vec2,
        scale: Vec2,
        glyph: Glyph,
        color: Color,
        useCamera: Boolean,
        snipRegion: SnipRegion?,
        angle: Float
    ) {
        val textureWidth = texture.width.toFloat()
        val textureHeight = texture.height.toFloat()
        val glyphScale = Vec2(glyph.width / textureWidth, glyph.height / textureHeight)
        val glyphOffset = Vec2((glyph.x + glyph.width) / textureWidth, glyph.y / textureHeight)
        val debug = oldRenderer.debugOffset

        val data = BasicShapes.square
            .flatMap { (x, y) ->
                listOf(
                    x * glyph.width, y * glyph.height, 0f,
                    0f, 0f, -1f,
                    color.red, color.green, color.blue, color.alpha,
                    (x * .5f - 0.5f) * glyphScale.x + glyphOffset.x * debug.x,
                    (y * .5f - 0.5f) * glyphScale.y + glyphOffset.y * debug.y
                )
            }.toFloatArray()

        texture.bind()
        oldRenderer.drawShape(data, offset, angle, scale, useCamera, snipRegion)

        //        textures.white_pixel.bind()
        //        renderer.drawShape(data, offset, 0f, scale, useCamera)
    }

    fun dispose() {
        fontBitMap.delete()
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
