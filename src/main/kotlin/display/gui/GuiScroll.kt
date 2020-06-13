package display.gui

import display.draw.Drawer
import display.draw.TextureEnum
import display.graphic.BasicShapes
import display.graphic.Color
import display.graphic.SnipRegion
import display.gui.LayoutController.setElementsInRows
import org.jbox2d.common.Vec2
import utility.PidController
import kotlin.math.absoluteValue

class GuiScroll(
    drawer: Drawer,
    offset: Vec2 = Vec2(),
    scale: Vec2 = Vec2(100f, 100f),
    color: Color = Color.WHITE.setAlpha(.5f),
    private val childElements: MutableList<GuiElement> = mutableListOf()
) : GuiElement(drawer, offset, scale, "", 0f, color, {}) {

    private var outline: FloatArray
    private val childElementOffsets = HashMap<GuiElement, Vec2>()

    private var scrollBarPosition = 0f
    private var scrollBarPositionTarget = scrollBarPosition
    private val scrollController = PidController(-.06f, -.0001f, -.08f)

    private var scrollBarMax: Float = 0f
    private var scrollBarMin: Float = 0f

    private var scrollBarOutline: FloatArray
    private var scrollBarOffset: Vec2
    private val scrollBarWidth = 6f

    init {
        val outlinePoints = BasicShapes.square
            .chunked(2)
            .flatMap { listOf(it[0] * scale.x, it[1] * scale.y) }
        outline = Drawer.getLine(outlinePoints, color, startWidth = 1f, wrapAround = true)

        val scrollBarPoints = BasicShapes.verticalLine
            .chunked(2)
            .flatMap { (x, y) -> listOf(x, y * scale.y * .5f) }
        scrollBarOutline = Drawer.getLine(scrollBarPoints, color, startWidth = scrollBarWidth)
        scrollBarOffset = getScrollBarElementOffset()

        //        childElementOffsets.putAll(childElements.map { Pair(it, it.offset.add(Vec2(-scrollBarWidth, 0f))) })
        calculateElementRegion(this)
        calculateNewOffsets()
    }

    override fun render(snipRegion: SnipRegion?) {
        // TODO: handle nested snipRegions, if this element is inside parent scroll
        drawer.textures.getTexture(TextureEnum.white_pixel).bind()
        drawer.renderer.drawStrip(outline, offset, useCamera = false, snipRegion = snipRegion)
        drawer.renderer.drawStrip(scrollBarOutline, scrollBarOffset, useCamera = false, snipRegion = snipRegion)
        super.render(snipRegion)

        childElements.filter {
            it.offset.sub(it.scale).y < offset.add(scale).y
                    && it.offset.add(it.scale).y > offset.sub(scale).y
        }
            .forEach { it.render(SnipRegion(offset.sub(scale), scale.mul(2f))) }
    }

    override fun update() {
        if (scrollBarPosition.minus(scrollBarPositionTarget).absoluteValue > .1f) {
            val movement = scrollController.getReaction(scrollBarPosition, scrollBarPositionTarget)
            scrollBarPosition = (scrollBarPosition + movement).coerceIn(scrollBarMin, scrollBarMax)
            calculateNewOffsets()
        }

        childElements.forEach { it.update() }
    }

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
        }
    }

    private fun addScrollBarPosition(movement: Float) {
        scrollBarPositionTarget =
            (scrollBarPositionTarget + movement * scale.y * 1.8f).coerceIn(scrollBarMin, scrollBarMax)
    }

    private fun updateScrollBarRange() {
        scrollBarMax = 0f
        scrollBarMin = childElements.minBy { it.offset.y }
            .let { childElementOffsets[it]!!.y + 2 * (scale.y - it!!.scale.y) }
    }

    private fun calculateNewOffsets() {
        scrollBarOffset = getScrollBarElementOffset()

        if (childElements.isEmpty()) return

        val firstElementScale = childElements.first().scale.y
        childElements.forEach {
            it.updateOffset(childElementOffsets[it]!!
                .add(offset)
                .add(Vec2(0f, scale.y - scrollBarPosition - firstElementScale)))
        }
    }

    private fun getScrollBarElementOffset(): Vec2 {
        val scrollBarPercentage = scrollBarPosition / scrollBarMin
        return offset.add(Vec2(scale.x - scrollBarWidth, scale.y * (.5f - scrollBarPercentage)))
    }

    fun addChildren(elements: List<GuiElement>): GuiScroll {
        elements.forEach { it.scale.addLocal(-scrollBarWidth, 0f) }
        childElements.addAll(elements)
        setElementsInRows(childElements, centered = false)

        childElementOffsets.putAll(elements.map {
            Pair(it, it.offset.add(Vec2(-scrollBarWidth * 2f, 0f)))
        })
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
