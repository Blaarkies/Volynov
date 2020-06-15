package display.gui

import display.draw.Drawer
import display.draw.TextureEnum
import display.graphic.BasicShapes
import display.graphic.Color
import display.graphic.SnipRegion
import display.text.TextJustify
import org.jbox2d.common.Vec2

class GuiButton(
    drawer: Drawer,
    offset: Vec2 = Vec2(),
    scale: Vec2 = Vec2(200f, 50f),
    title: String = "",
    textSize: Float = .2f,
    color: Color = Color.WHITE.setAlpha(.7f),
    private val onClick: () -> Unit = {},
    updateCallback: (GuiElement) -> Unit = {}
) : GuiElement(drawer, offset, scale, title, textSize, color, updateCallback) {

    private var isPressed = false
    private var outline: FloatArray
    private var background: FloatArray
    private var backgroundColor = color.setAlpha(.1f)

    init {
        val linePoints = BasicShapes.square
            .chunked(2)
            .flatMap { listOf(it[0] * scale.x, it[1] * scale.y) }
        outline = Drawer.getLine(linePoints, color, startWidth = 1f, wrapAround = true)
        background = Drawer.getColoredData(linePoints, backgroundColor).toFloatArray()

        calculateElementRegion(this)
    }

    override fun render(snipRegion: SnipRegion?) {
        drawer.textures.getTexture(TextureEnum.white_pixel).bind()

        when (currentPhase) {
            GuiElementPhases.HOVER ->
                drawer.renderer.drawShape(background, offset, useCamera = false, snipRegion = snipRegion)
        }

        when (currentPhase) {
            GuiElementPhases.ACTIVE ->
                drawer.renderer.drawStrip(outline, offset,
                    scale = Vec2((scale.x - 2f) / scale.x, (scale.y - 2f) / scale.y),
                    useCamera = false, snipRegion = snipRegion)
            else ->
                drawer.renderer.drawStrip(outline, offset, useCamera = false, snipRegion = snipRegion)
        }

        super.render(snipRegion)
        drawLabel(drawer, this, TextJustify.CENTER, snipRegion)
    }

    override fun handleHover(location: Vec2) {
        if (isPressed) return
        super.handleHover(location)
    }

    override fun handleLeftClickPress(location: Vec2): Boolean {
        val isHovered = isHover(location)
        if (isHovered) {
            isPressed = true
            currentPhase = GuiElementPhases.ACTIVE
        }
        return isHovered
    }

    override fun handleLeftClickRelease(location: Vec2): Boolean {
        if (!isPressed) return false

        if (isHover(location)) {
            onClick()
        }
        isPressed = false
        currentPhase = GuiElementPhases.IDLE
        return true
    }

}
