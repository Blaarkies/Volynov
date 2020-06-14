package display.gui

import display.draw.Drawer
import display.draw.TextureEnum
import display.graphic.BasicShapes
import display.graphic.Color
import display.graphic.SnipRegion
import display.text.TextJustify
import org.jbox2d.common.Vec2
import utility.Common

class GuiPanel(
    drawer: Drawer,
    offset: Vec2 = Vec2(),
    scale: Vec2 = Vec2(100f, 100f),
    title: String = "",
    textSize: Float = .3f,
    color: Color = Color.BLACK.setAlpha(.5f),
    private val childElements: MutableList<GuiElement> = mutableListOf(),
    private val draggable: Boolean = true,
    updateCallback: (GuiElement) -> Unit = {}
) : GuiElement(drawer, offset, scale, title, textSize, color, updateCallback) {

    private val childElementOffsets = HashMap<GuiElement, Vec2>()

    init {
        childElementOffsets.putAll(childElements.map { Pair(it, it.offset.clone()) })
        calculateElementRegion(this)
    }

    override fun render(snipRegion: SnipRegion?) {
        drawer.textures.getTexture(TextureEnum.white_pixel).bind()
        drawer.renderer.drawShape(BasicShapes.square
            .let { Drawer.getColoredData(it, color) }
            .toFloatArray(),
            offset, 0f, scale, useCamera = false, snipRegion = snipRegion
        )

        drawer.renderer.drawText(
            title,
            offset.add(Vec2(0f, scale.y - 25f)),
            Common.vectorUnit.mul(.15f),
            Color.WHITE,
            TextJustify.CENTER,
            false,
            snipRegion
        )

        childElements.forEach { it.render(snipRegion) }

        super.render(snipRegion)
    }

    override fun update() = childElements.forEach { it.update() }

    override fun addOffset(newOffset: Vec2) {
        addOffset(this, newOffset)
        calculateNewOffsets()
    }

    override fun updateOffset(newOffset: Vec2) {
        updateOffset(this, newOffset)
        calculateNewOffsets()
    }

    override fun handleHover(location: Vec2) {
        if (isHover(location)) {
            childElements.forEach { it.handleHover(location) }
        }
    }

    override fun handleLeftClickPress(location: Vec2): Boolean {
        return isHover(location)
                && childElements.any { it.handleLeftClickPress(location) }
    }

    override fun handleLeftClickRelease(location: Vec2): Boolean {
        return childElements.any { it.handleLeftClickRelease(location) }
    }

    override fun handleLeftClickDrag(location: Vec2, movement: Vec2) {
        if (draggable && isHover(location)) { // TODO: use custom isHover() to only allow a small region as drag handle
            childElements.forEach { it.handleLeftClickDrag(location, movement) }
            super.handleLeftClickDrag(location, movement)
            addOffset(movement)
        }
    }

    override fun handleScroll(location: Vec2, movement: Vec2) {
        if (isHover(location)) {
            super.handleScroll(location, movement)
            childElements.forEach { it.handleScroll(location, movement) }
        }
    }

    private fun calculateNewOffsets() = childElements.forEach { it.updateOffset(childElementOffsets[it]!!.add(offset)) }

    fun addChildren(elements: List<GuiElement>) {
        childElements.addAll(elements)
        childElementOffsets.putAll(elements.map { Pair(it, it.offset.clone()) })
        calculateNewOffsets()
    }

    fun addChild(element: GuiElement) {
        childElements.add(element)
        childElementOffsets[element] = element.offset.clone()
        calculateNewOffsets()
    }

}
