package display.gui

import display.draw.Drawer
import display.draw.TextureEnum
import display.graphic.BasicShapes
import display.graphic.Color
import display.graphic.SnipRegion
import display.text.TextJustify
import org.jbox2d.common.Vec2
import utility.Common.makeVec2
import utility.Common.vectorUnit
import utility.toList

class GuiInput(
    drawer: Drawer,
    offset: Vec2 = Vec2(),
    scale: Vec2 = Vec2(200f, 50f),
    placeholder: String,
    textSize: Float = .15f,
    color: Color = Color.WHITE.setAlpha(.7f),
    private val onClick: () -> Unit = {},
    updateCallback: (GuiElement) -> Unit = {},
    val onChange: (String) -> Unit
) : GuiElement(drawer, offset, scale, placeholder, textSize, color, updateCallback) {

    private val blinkRate = 400
    private var cursorLine: FloatArray
    private var buttonOutline: FloatArray
    private var buttonBackground: FloatArray
    private var backgroundColor = color.setAlpha(.1f)

    private var inputText = ""
    private val paddedScale = Vec2(scale.x - 8f, 20f)
    val textInputIsBusy
        get() = currentPhase == GuiElementPhases.INPUT

    init {
        val verticalCursorLinePoint = BasicShapes.verticalLine.chunked(2)
            .flatMap {
                val location = makeVec2(it[0] * paddedScale.x, it[1] * paddedScale.y)
                    .also { vec -> vec.x -= paddedScale.x }
                location.toList()
            }
        cursorLine = Drawer.getLine(verticalCursorLinePoint, color, startWidth = 1.2f)

        val linePoints = BasicShapes.square.chunked(2)
            .flatMap { listOf(it[0] * scale.x, it[1] * scale.y) }
        buttonOutline = Drawer.getLine(linePoints, color, startWidth = 1f, wrapAround = true)
        buttonBackground = Drawer.getColoredData(linePoints, backgroundColor).toFloatArray()

        calculateElementRegion(this)
    }

    override fun render(snipRegion: SnipRegion?) {
        drawer.textures.getTexture(TextureEnum.white_pixel).bind()

        when (currentPhase) {
            GuiElementPhases.HOVERED -> drawer.renderer.drawShape(buttonBackground, offset, useCamera = false,
                snipRegion = snipRegion)
            GuiElementPhases.INPUT -> {
                drawer.renderer.drawShape(buttonBackground, offset, useCamera = false, snipRegion = snipRegion)

                if (System.currentTimeMillis().rem(blinkRate * 2) < blinkRate) {
                    drawer.renderer.drawStrip(cursorLine, offset, useCamera = false, snipRegion = snipRegion)
                }
            }
        }

        drawer.renderer.drawStrip(buttonOutline, offset, useCamera = false)

        val paddedOffset = offset.clone().also { it.x -= paddedScale.x }
        when (inputText.length) {
            0 -> drawer.renderer.drawText(title, paddedOffset, vectorUnit.mul(textSize),
                color.setAlpha(.4f), TextJustify.LEFT, false, snipRegion)
            else -> drawer.renderer.drawText(inputText, paddedOffset, vectorUnit.mul(textSize),
                color, TextJustify.LEFT, false, snipRegion)
        }
        super.render(snipRegion)
    }

    override fun handleHover(location: Vec2) {
        if (textInputIsBusy) return
        super.handleHover(location)
    }

    override fun handleLeftClick(location: Vec2) {
        when {
            isHover(location) -> {
                currentPhase = GuiElementPhases.INPUT
                onClick()
            }
            else -> currentPhase = GuiElementPhases.IDLE
        }
    }

    fun handleAddTextInput(text: String) {
        if (textInputIsBusy) {
            inputText += text
            onChange(inputText)
        }
    }

    fun handleRemoveTextInput() {
        if (textInputIsBusy) {
            inputText = inputText.dropLast(1)
            onChange(inputText)
        }
    }

    fun stopTextInput() {
        if (textInputIsBusy) {
            currentPhase = GuiElementPhases.IDLE
        }
    }

    fun setTextValue(text: String): GuiInput {
        inputText = text
        onChange(inputText)
        return this
    }

}
