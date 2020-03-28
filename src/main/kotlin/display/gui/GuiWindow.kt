package display.gui

import display.draw.Drawer
import display.graphic.BasicShapes
import display.graphic.Color
import org.jbox2d.common.Vec2
import utility.Common

class GuiWindow(
    drawer: Drawer,
    offset: Vec2 = Vec2(),
    scale: Vec2 = Vec2(100f, 100f),
    title: String = " ",
    textSize: Float = .3f,
    color: Color = Color.BLACK.setAlpha(.5f),
    private val childElements: MutableList<GuiElement> = mutableListOf(),
    private val draggable: Boolean = true
) : GuiElement(drawer, offset, scale, title, textSize, color) {

    private val childElementOffsets = HashMap<GuiElement, Vec2>()

    init {
        childElementOffsets.putAll(childElements.map { Pair(it, it.offset.clone()) })
    }

    override fun render() {
        drawer.textures.white_pixel.bind()
        drawer.renderer.drawShape(BasicShapes.square
            .let { Drawer.getColoredData(it, color) }
            .toFloatArray(),
            offset, 0f, scale, useCamera = false
        )

        drawer.renderer.drawText(
            title,
            offset.add(Vec2(0f, scale.y - 25f)),
            Common.vectorUnit.mul(.15f),
            Color.WHITE,
            false
        )

        childElements.forEach { it.render() }

        super.render()
    }

    override fun addOffset(newOffset: Vec2) {
        GuiElement.addOffset(this, newOffset)
        update()
    }

    override fun handleHover(location: Vec2) {
        childElements.forEach { it.handleHover(location) }
    }

    override fun handleClick(location: Vec2) {
        childElements.forEach { it.handleClick(location) }
    }

    fun update() {
        childElements.forEach { it.updateOffset(childElementOffsets[it]!!.add(offset)) }
    }

    fun addChildren(elements: List<GuiElement>) {
        childElements.addAll(elements)
        childElementOffsets.putAll(elements.map { Pair(it, it.offset.clone()) })
        update()
    }

    fun addChild(element: GuiElement) {
        childElements.add(element)
        childElementOffsets[element] = element.offset.clone()
        update()
    }

}
