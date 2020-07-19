package display.gui.elements

import dI
import display.draw.Drawer
import display.draw.TextureEnum
import display.events.MouseButtonEvent
import display.graphic.BasicShapes
import display.graphic.Color
import display.graphic.SnipRegion
import display.gui.base.GuiElement
import display.gui.base.GuiElementIdentifierType
import display.gui.base.GuiElementPhase.*
import display.gui.base.HasClick
import display.text.TextJustify
import io.reactivex.Observable
import io.reactivex.Observable.merge
import org.jbox2d.common.Vec2
import utility.Common.makeVec2
import utility.Common.vectorUnit
import utility.toList

class GuiInput(
    override val offset: Vec2 = Vec2(),
    override val scale: Vec2 = Vec2(200f, 50f),
    val placeholder: String,
    private val textSize: Float = .15f,
    override var color: Color = Color.WHITE.setAlpha(.7f),
    override val onClick: () -> Unit = {},
    override val updateCallback: (GuiElement) -> Unit = {},
    val onChange: (String) -> Unit
) : HasClick {

    override var topRight = Vec2()
    override var bottomLeft = Vec2()
    override var id = GuiElementIdentifierType.DEFAULT
    override var currentPhase = IDLE

    private val blinkRate = 400
    private var cursorLine: FloatArray
    private var buttonOutline: FloatArray
    private var buttonBackground: FloatArray
    private var backgroundColor = color.setAlpha(.1f)

    private var inputText = ""
    private val paddedScale = Vec2(scale.x - 8f, 20f)

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
            HOVER -> dI.renderer.drawShape(buttonBackground, offset, useCamera = false, snipRegion = parentSnipRegion)
            ACTIVE -> {
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

    override fun handleLeftClick(startEvent: MouseButtonEvent, event: Observable<MouseButtonEvent>): Boolean {
        val isHovered = isHover(startEvent.location)
        if (isHovered) {
            setActive()
        }
        return isHovered
    }

    fun setTextValue(text: String): GuiInput {
        inputText = text
        onChange(inputText)
        return this
    }

    fun setActive() {
        currentPhase = ACTIVE

        val mouseLeftClickEvent = dI.inputHandler.mouseButtonEvent.doOnNext { it.toScreen() }
            .filter { it.isLeft && it.isPress && !isHover(it.location) }

        val keyPressEvent = dI.inputHandler.keyboardEvent.filter { it.isPress }

        val enterKeyEvent = keyPressEvent.filter { it.isEnter }
        val escapeKeyEvent = keyPressEvent.filter { it.isEscape }
        val tabKeyEvent = keyPressEvent.filter { it.isTab }
            .doAfterNext { dI.guiController.cycleActiveElement(this, it.shiftHeld) }
        val backspaceKeyEvent = keyPressEvent.filter { it.isBackspace }
            .doOnNext { inputText = inputText.dropLast(1) }

        val endInputEvent = merge(mouseLeftClickEvent, enterKeyEvent, tabKeyEvent, escapeKeyEvent)
            .take(1)
            .doOnNext { currentPhase = IDLE }

        val textInputEvent = dI.inputHandler.textInputEvent.doOnNext { text -> inputText += text }

        merge(textInputEvent, backspaceKeyEvent)
            .takeUntil(endInputEvent)
            .subscribe { onChange(inputText) }
    }

}
