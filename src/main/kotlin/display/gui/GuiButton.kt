package display.gui

import display.draw.Drawer
import display.graphic.BasicShapes
import display.graphic.Color
import org.jbox2d.common.Vec2

class GuiButton(
    drawer: Drawer,
    title: String,
    offset: Vec2 = Vec2(),
    textSize: Float = 0f,
    color: Color = Color.WHITE.setAlpha(.7f),
    private val scale: Vec2 = Vec2(200f, 50f),
    private val onClick: () -> Unit
) : GuiElement(drawer, title, offset, textSize, color) {

    private var buttonOutline: FloatArray
    private var buttonBackground: FloatArray
    private var backgroundColor = color.setAlpha(.1f)

    private var topRight: Vec2
    private var bottomLeft: Vec2

    init {
        val linePoints = BasicShapes.square
            .chunked(2)
            .flatMap { listOf(it[0] * scale.x, it[1] * scale.y) }
        buttonOutline = Drawer.getLine(linePoints, color, startWidth = 1f, wrapAround = true)
        buttonBackground = Drawer.getColoredData(linePoints, backgroundColor).toFloatArray()

        bottomLeft = offset.add(scale.negate())
        topRight = offset.add(scale)
    }

    override fun render() {
        drawer.textures.white_pixel.bind()
        when (currentPhase) {
            GuiElementPhases.HOVERED -> drawer.renderer.drawShape(buttonBackground, offset, useCamera = false)
        }

        when (currentPhase) {
            GuiElementPhases.CLICKED -> drawer.renderer.drawStrip(
                buttonOutline,
                offset.add(Vec2(0f, -2f)),
                useCamera = false
            )
            else -> drawer.renderer.drawStrip(buttonOutline, offset, useCamera = false)
        }

        drawer.renderer.drawText(
            title, offset, vectorIdentity.mul(textSize),
            color, false
        )
    }

    fun handleHover(location: Vec2) {
        when {
            isHover(location) -> currentPhase = GuiElementPhases.HOVERED
            else -> currentPhase = GuiElementPhases.IDLE
        }
    }


    fun handleClick(location: Vec2) {
        when {
            isHover(location) -> {
                currentPhase = GuiElementPhases.CLICKED
                onClick()
            }
            else -> currentPhase = GuiElementPhases.IDLE
        }
    }

    private fun isHover(location: Vec2): Boolean {
        return location.x > bottomLeft.x
                && location.x < topRight.x
                && location.y > bottomLeft.y
                && location.y < topRight.y
    }


}
