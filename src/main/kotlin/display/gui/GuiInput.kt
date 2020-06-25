package display.gui

import dI
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
    override val offset: Vec2 = Vec2(),
    override val scale: Vec2 = Vec2(200f, 50f),
    val placeholder: String,
    private val textSize: Float = .15f,
    override val color: Color = Color.WHITE.setAlpha(.7f),
    override val onClick: () -> Unit = {},
    override val updateCallback: (GuiElement) -> Unit = {},
    val onChange: (String) -> Unit
) : HasClick {

    override var isPressed = false
    override var topRight = Vec2()
    override var bottomLeft = Vec2()
    override var id = GuiElementIdentifierType.DEFAULT
    override var currentPhase = GuiElementPhases.IDLE

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

        calculateElementRegion()
    }

    override fun render(parentSnipRegion: SnipRegion?) {
        dI.textures.getTexture(TextureEnum.white_pixel).bind()

        when (currentPhase) {
            GuiElementPhases.HOVER ->
                dI.renderer.drawShape(buttonBackground, offset, useCamera = false, snipRegion = parentSnipRegion)
            GuiElementPhases.INPUT -> {
                dI.renderer.drawShape(buttonBackground, offset, useCamera = false, snipRegion = parentSnipRegion)

                if (System.currentTimeMillis().rem(blinkRate * 2) < blinkRate) {
                    dI.renderer.drawStrip(cursorLine, offset, useCamera = false, snipRegion = parentSnipRegion)
                }
            }
        }

        dI.renderer.drawStrip(buttonOutline, offset, useCamera = false)

        val paddedOffset = offset.clone().also { it.x -= paddedScale.x }
        when (inputText.length) {
            0 -> dI.renderer.drawText(placeholder, paddedOffset, vectorUnit.mul(textSize),
                color.setAlpha(.4f), TextJustify.LEFT, false, parentSnipRegion)
            else -> dI.renderer.drawText(inputText, paddedOffset, vectorUnit.mul(textSize),
                color, TextJustify.LEFT, false, parentSnipRegion)
        }
        super.render(parentSnipRegion)
    }

    override fun handleHover(location: Vec2): Boolean {
        return when {
            textInputIsBusy -> false
            super.isHover(location) -> {
                currentPhase = GuiElementPhases.HOVER
                true
            }
            else -> {
                currentPhase = GuiElementPhases.IDLE
                false
            }
        }
    }

    override fun handleLeftClickPress(location: Vec2): Boolean {
        val isHovered = super.handleLeftClickPress(location)
        if (!isHovered) {
            currentPhase = GuiElementPhases.IDLE
        }
        return isHovered
    }

    override fun handleLeftClickRelease(location: Vec2): Boolean {
        if (!isPressed) return false

        if (isHover(location)) {
            currentPhase = GuiElementPhases.INPUT
            onClick()
        }
        isPressed = false
        return true
    }

    fun handleAddTextInput(text: String): Boolean {
        return when {
            textInputIsBusy -> {
                inputText += text
                onChange(inputText)
                true
            }
            else -> false
        }
    }

    fun handleRemoveTextInput(): Boolean {
        return when {
            textInputIsBusy -> {
                inputText = inputText.dropLast(1)
                onChange(inputText)
                true
            }
            else -> false
        }
    }

    fun stopTextInput(): Boolean {
        return when {
            textInputIsBusy -> {
                currentPhase = GuiElementPhases.IDLE
                true
            }
            else -> false
        }
    }

    fun setTextValue(text: String): GuiInput {
        inputText = text
        onChange(inputText)
        return this
    }

}
