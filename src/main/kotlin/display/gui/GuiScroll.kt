package display.gui

import display.draw.Drawer
import display.draw.TextureEnum
import display.graphic.BasicShapes
import display.graphic.Color
import display.graphic.SnipRegion
import display.gui.LayoutController.getOffsetForLayoutPosition
import display.gui.LayoutController.setElementsInRows
import org.jbox2d.common.Vec2
import utility.Common.makeVec2
import utility.PidController
import utility.toSign
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
    private var isPressed = false
    private var isThumbed = false

    private var scrollBarPosition = 0f
    private var scrollBarPositionTarget = scrollBarPosition
    private val scrollController = PidController(-.06f, -.0001f, -.08f)

    private var scrollBarMax: Float = 0f
    private var scrollBarMin: Float = 0f
    private var scrollBarRegionScale: Vec2
    private var scrollBarRegionRelativeOffset: Vec2
    private lateinit var scrollBarRegionTopRight: Vec2
    private lateinit var scrollBarRegionBottomLeft: Vec2

    private val thumb: GuiIcon
    private val thumbScale: Vec2
    private var thumbRelativeOffset: Vec2
    private lateinit var thumbTopRight: Vec2
    private lateinit var thumbBottomLeft: Vec2

    init {
        val outlinePoints = BasicShapes.square
            .chunked(2)
            .flatMap { listOf(it[0] * scale.x, it[1] * scale.y) }
        outline = Drawer.getLine(outlinePoints, color, startWidth = 1f, wrapAround = true)

        thumbScale = Vec2(6f, scale.y * .5f)
        thumbRelativeOffset = getOffsetForLayoutPosition(LayoutPosition.TOP_RIGHT, scale, thumbScale)
        thumb = GuiIcon(drawer, scale = thumbScale, color = color, texture = TextureEnum.white_pixel)

        scrollBarRegionScale = Vec2(thumbScale.x, scale.y * 2f)
        scrollBarRegionRelativeOffset = Vec2(scale.x - thumbScale.x, 0f)

        childElementOffsets.putAll(childElements.map {
            Pair(it, it.offset.add(Vec2(-thumbScale.x * 2, 0f)))
        })
        calculateElementRegion(this)
        calculateThumbRegion()
    }

    override fun render(snipRegion: SnipRegion?) {
        drawer.textures.getTexture(TextureEnum.white_pixel).bind()
        drawer.renderer.drawStrip(outline, offset, useCamera = false, snipRegion = snipRegion)
        super.render(snipRegion)

        childElements.filter {
            it.offset.sub(it.scale).y < offset.add(scale).y
                    && it.offset.add(it.scale).y > offset.sub(scale).y
        }
            .forEach { it.render(SnipRegion(offset.sub(scale), scale.mul(2f))) }
        thumb.render(snipRegion)
    }

    override fun update() {
        if (scrollBarPosition.minus(scrollBarPositionTarget).absoluteValue > .1f) {
            val movement = scrollController.getReaction(scrollBarPosition, scrollBarPositionTarget)
            scrollBarPosition = (scrollBarPosition + movement).coerceIn(scrollBarMin, scrollBarMax)
            calculateThumbRegion()
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
            childElements.forEach { it.handleHover(location) }
        }
    }

    override fun handleLeftClickPress(location: Vec2): Boolean {
        return when {
            isThumbRegion(location) -> {
                isThumbed = true
                true
            }
            isScrollbarRegion(location) -> {
                val isAbove = location.y > thumb.offset.y
                addScrollBarPosition(isAbove.toSign() * scale.y * 1.8f * 3f)
                true
            }
            isHover(location) -> {
                isPressed = true
                childElements.any { it.handleLeftClickPress(location) }
            }
            else -> false
        }
    }

    override fun handleLeftClickRelease(location: Vec2): Boolean {
        isThumbed = false
        isPressed = false
        return childElements.any { it.handleLeftClickRelease(location) }
    }

    override fun handleLeftClickDrag(location: Vec2, movement: Vec2): Boolean {
        return when {
            isThumbed -> {
                addScrollBarPosition(-(movement.y / scale.y) * scrollBarMin)
                calculateNewOffsets()
                true
            }
            isPressed && isHover(location) -> {
                addScrollBarPosition(-movement.y)
                // TODO: slow drag should not cause button click on release. Reset inner button "isPressed" status
                childElements.forEach { it.handleLeftClickRelease(makeVec2(9999)) }
                calculateNewOffsets()
                true
            }
            else -> false
        }
    }

    override fun handleScroll(location: Vec2, movement: Vec2): Boolean {
        return when {
            isHover(location) -> {
                addScrollBarPosition(movement.y * scale.y * 1.8f)
                true
            }
            else -> false
        }
    }

    private fun addScrollBarPosition(movement: Float) {
        scrollBarPositionTarget = (scrollBarPositionTarget + movement).coerceIn(scrollBarMin, scrollBarMax)
    }

    private fun updateScrollBarRange() {
        scrollBarMax = 0f
        scrollBarMin = childElements.minBy { it.offset.y }
            .let { childElementOffsets[it]!!.y + 2 * (scale.y - it!!.scale.y) }
    }

    private fun calculateNewOffsets() {
        if (childElements.isEmpty()) return

        val scrollPercentage = if (scrollBarMin != 0f) scrollBarPosition / scrollBarMin else 0f
        thumbRelativeOffset = Vec2(thumbRelativeOffset.x, scale.y * (.5f - scrollPercentage))
        thumb.updateOffset(thumbRelativeOffset.add(offset))
        calculateThumbRegion()

        val firstElementScale = childElements.first().scale.y
        childElements.forEach {
            it.updateOffset(childElementOffsets[it]!!
                .add(offset)
                .add(Vec2(0f, scale.y - scrollBarPosition - firstElementScale)))
        }
    }

    private fun calculateThumbRegion() {
        thumbTopRight = thumb.offset.add(thumbScale)
        thumbBottomLeft = thumb.offset.sub(thumbScale)

        scrollBarRegionTopRight = offset.add(scrollBarRegionRelativeOffset).add(scrollBarRegionScale)
        scrollBarRegionBottomLeft = offset.add(scrollBarRegionRelativeOffset).sub(scrollBarRegionScale)
    }

    private fun isThumbRegion(location: Vec2): Boolean =
        isInRegion(location, thumbBottomLeft, thumbTopRight)

    private fun isScrollbarRegion(location: Vec2): Boolean =
        isInRegion(location, scrollBarRegionBottomLeft, scrollBarRegionTopRight)

    fun addChildren(elements: List<GuiElement>): GuiScroll {
        val thumbWidth = thumbScale.x * 2
        elements.forEach { it.scale.addLocal(-thumbWidth, 0f) }
        childElements.addAll(elements)
        setElementsInRows(childElements, centered = false)

        childElementOffsets.putAll(elements.map {
            Pair(it, it.offset.add(Vec2(-thumbWidth, 0f)))
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
