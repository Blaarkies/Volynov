package display.gui

import display.draw.Drawer
import display.draw.TextureEnum
import display.graphic.BasicShapes
import display.graphic.Color
import display.graphic.SnipRegion
import display.gui.GuiController.Companion.setElementsInRows
import org.jbox2d.common.Vec2

class GuiScroll(
    drawer: Drawer,
    offset: Vec2 = Vec2(),
    scale: Vec2 = Vec2(100f, 100f),
    color: Color = Color.WHITE.setAlpha(.5f),
    private val childElements: MutableList<GuiElement> = mutableListOf()
) : GuiElement(drawer, offset, scale, "", 0f, color, {}) {

    private var scrollOutline: FloatArray
    private val childElementOffsets = HashMap<GuiElement, Vec2>()

    private var scrollBarPosition = 0f
    private var scrollBarMin: Float = 0f
    private var scrollBarMax: Float = 0f

    init {
        val linePoints = BasicShapes.square
            .chunked(2)
            .flatMap { listOf(it[0] * scale.x, it[1] * scale.y) }
        scrollOutline = Drawer.getLine(linePoints, color, startWidth = 1f, wrapAround = true)

        childElementOffsets.putAll(childElements.map { Pair(it, it.offset.clone()) })
        calculateElementRegion(this)
        calculateNewOffsets()
    }

    override fun render(snipRegion: SnipRegion?) {
        // TODO: handle nested snipRegions, if this element is inside parent scroll
        drawer.textures.getTexture(TextureEnum.white_pixel).bind()
        drawer.renderer.drawStrip(scrollOutline, offset, useCamera = false, snipRegion = snipRegion)
        super.render(snipRegion)

        childElements.filter {
            it.offset.add(it.scale.negate()).y < offset.add(scale).y
                    && it.offset.add(it.scale).y > offset.add(scale.negate()).y
        }
            .forEach { it.render(SnipRegion(offset.add(scale.negate()), scale.mul(2f))) }
    }

    override fun update() = childElements.forEach { it.update() }

    override fun addOffset(newOffset: Vec2) {
        addOffset(this, newOffset)
        calculateNewOffsets()
    }

    override fun updateOffset(newOffset: Vec2) {
        super.updateOffset(newOffset)
        calculateNewOffsets()
    }

    override fun handleHover(location: Vec2) {
        if (isHover(location)) {
            super.handleHover(location)
            childElements.forEach { it.handleHover(location) }
        }
    }

    override fun handleLeftClick(location: Vec2) {
        if (isHover(location)) {
            super.handleLeftClick(location)
            childElements.forEach { it.handleLeftClick(location) }
        }
    }

    override fun handleLeftClickDrag(location: Vec2, movement: Vec2) {
        if (isHover(location)) {
            addScrollBarPosition(movement.y * -.1f)
            calculateNewOffsets()
            super.handleLeftClickDrag(location, movement)
            childElements.forEach { it.handleLeftClickDrag(location, movement) }
        }
    }

    override fun handleScroll(location: Vec2, movement: Vec2) {
        if (isHover(location)) {
            addScrollBarPosition(movement.y)
            calculateNewOffsets()
        }
    }

    private fun addScrollBarPosition(movement: Float) {
        scrollBarPosition = (scrollBarPosition + movement * 10f).coerceIn(scrollBarMin, scrollBarMax)
    }

    private fun updateScrollBarRange() {
        scrollBarMin = childElements.minBy { it.offset.y }
            .let { childElementOffsets[it]!!.y + scale.y * 2 - it!!.scale.y }
        scrollBarMax = 0f
    }

    private fun calculateNewOffsets() {
        childElements.forEach {
            it.updateOffset(childElementOffsets[it]!!
                .add(offset)
                .add(Vec2(0f, scale.y - scrollBarPosition)))
        }
    }

    fun addChildren(elements: List<GuiElement>): GuiScroll {
        childElements.addAll(elements)
        setElementsInRows(childElements, centered = false)

        childElementOffsets.putAll(elements.map { Pair(it, it.offset.clone()) })
        calculateNewOffsets()
        updateScrollBarRange()
        return this
    }

    fun addChild(element: GuiElement): GuiScroll {
        childElements.add(element)
        setElementsInRows(childElements)

        childElementOffsets[element] = element.offset.clone()
        calculateNewOffsets()
        updateScrollBarRange()
        return this
    }

}
