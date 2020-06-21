package display.gui

import dI
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

class GuiScroll(
    override val offset: Vec2 = Vec2(),
    override val scale: Vec2 = Vec2(100f, 100f),
    override val color: Color = Color.WHITE.setAlpha(.5f),
    override val kidElements: MutableList<GuiElement> = mutableListOf()
) : HasKids, HasScroll {

    override var id = GuiElementIdentifierType.DEFAULT
    override var currentPhase = GuiElementPhases.IDLE
    override val updateCallback: (GuiElement) -> Unit = {}
    override var topRight = Vec2()
    override var bottomLeft = Vec2()
    override val onClick: () -> Unit = {}

    private var outline: FloatArray
    private lateinit var snipRegion: SnipRegion
    override val kidElementOffsets = HashMap<GuiElement, Vec2>()
    override var isPressed = false
    private var isThumbed = false

    override var scrollBarPosition = 0f
    override var scrollBarPositionTarget = scrollBarPosition
    override val scrollController = PidController(-.06f, -.0001f, -.08f)
    override var scrollBarMax: Float = 0f
    override var scrollBarMin: Float = 0f

    private var scrollBarRegionScale: Vec2
    private var scrollBarRegionRelativeOffset: Vec2
    private lateinit var scrollBarRegionTopRight: Vec2
    private lateinit var scrollBarRegionBottomLeft: Vec2

    private val thumb: GuiIcon
    private val thumbScale: Vec2
    private var thumbRelativeOffset: Vec2
    private lateinit var thumbTopRight: Vec2
    private lateinit var thumbBottomLeft: Vec2

    private var lastMouseLocation = offset.sub(scale).sub(makeVec2(1))

    init {
        val outlinePoints = BasicShapes.square
            .chunked(2)
            .flatMap { listOf(it[0] * scale.x, it[1] * scale.y) }
        outline = Drawer.getLine(outlinePoints, color, startWidth = 1f, wrapAround = true)

        thumbScale = Vec2(6f, scale.y * .5f)
        thumbRelativeOffset = getOffsetForLayoutPosition(LayoutPosition.TOP_RIGHT, scale, thumbScale)
        thumb = GuiIcon(scale = thumbScale, color = color)

        scrollBarRegionScale = Vec2(thumbScale.x, scale.y * 2f)
        scrollBarRegionRelativeOffset = Vec2(scale.x - thumbScale.x, 0f)

        kidElementOffsets.putAll(kidElements.map {
            Pair(it, it.offset.add(Vec2(-thumbScale.x * 2, 0f)))
        })
        calculateElementRegion()
        calculateThumbRegion()
        calculateSnipRegion()
    }

    override fun render(parentSnipRegion: SnipRegion?) {
        dI.textures.getTexture(TextureEnum.white_pixel).bind()
        dI.renderer.drawStrip(outline, offset, useCamera = false, snipRegion = snipRegion)

        kidElements.filter {
            it.offset.sub(it.scale).y < offset.add(scale).y
                    && it.offset.add(it.scale).y > offset.sub(scale).y
        }.forEach { it.render(snipRegion) }

        thumb.render(snipRegion)
        super<HasKids>.render(snipRegion)
    }

    override fun update() {
        super<HasScroll>.update()
        super<HasKids>.update()
        super<HasKids>.handleHover(lastMouseLocation)
    }

    override fun handleHover(location: Vec2): Boolean {
        lastMouseLocation = location
        return super<HasKids>.handleHover(location)
    }

    override fun scrollBarOnMove() {
        calculateThumbRegion()
        calculateNewOffsets()
        super.scrollBarOnMove()
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
            super<HasKids>.isHover(location) -> {
                isPressed = true
                super<HasKids>.handleLeftClickPress(location)
            }
            else -> false
        }
    }

    override fun handleLeftClickRelease(location: Vec2): Boolean {
        isThumbed = false
        return super<HasKids>.handleLeftClickRelease(location)
    }

    override fun handleLeftClickDrag(location: Vec2, movement: Vec2): Boolean {
        return when {
            isThumbed -> {
                addScrollBarPosition(-(movement.y / scale.y) * scrollBarMin)
                calculateNewOffsets()
                true
            }
            super<HasScroll>.handleLeftClickDrag(location, movement) -> {
                // TODO: slow drag should not cause button click on release. Reset inner button "isPressed" status
                kidElements.filterIsInstance<HasClick>()
                    .forEach { it.handleLeftClickRelease(it.offset.sub(it.scale).sub(makeVec2(1f))) }
                calculateNewOffsets()
                true
            }
            else -> false
        }
    }

    override fun handleScroll(location: Vec2, movement: Vec2): Boolean =
        super<HasScroll>.handleScroll(location, movement)

    override fun updateScrollBarRange() {
        super.updateScrollBarRange()
        scrollBarMax = 0f
        scrollBarMin = kidElements.minBy { it.offset.y }
            .let { kidElementOffsets[it]!!.y + 2 * (scale.y - it!!.scale.y) }
    }

    private fun calculateSnipRegion() {
        snipRegion = SnipRegion(offset.sub(scale), scale.mul(2f))
    }

    override fun calculateNewOffsets() {
        if (kidElements.isEmpty()) return

        calculateSnipRegion()

        val scrollPercentage = if (scrollBarMin != 0f) scrollBarPosition / scrollBarMin else 0f
        thumbRelativeOffset = Vec2(thumbRelativeOffset.x, scale.y * (.5f - scrollPercentage))
        thumb.updateOffset(thumbRelativeOffset.add(offset))
        calculateThumbRegion()

        val firstElementScale = kidElements.first().scale.y
        kidElements.forEach {
            it.updateOffset(kidElementOffsets[it]!!
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

    override fun addKids(kids: List<GuiElement>): GuiScroll {
        val thumbHalfWidth = thumbScale.x
        kids.forEach { it.updateScale(it.scale.sub(Vec2(thumbHalfWidth, 0f))) }

        kidElements.addAll(kids)
        setElementsInRows(kidElements, centered = false)

        kidElementOffsets.putAll(kids.map {
            Pair(it, it.offset.sub(Vec2(thumbHalfWidth, 0f)))
        })
        calculateNewOffsets()
        updateScrollBarRange()
        return this
    }

    override fun addKid(kid: GuiElement): GuiScroll = addKids(listOf(kid))

}
