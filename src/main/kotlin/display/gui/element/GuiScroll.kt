package display.gui.element

import dI
import display.draw.Drawer
import display.draw.TextureEnum
import display.event.DistanceCalculator
import display.event.MouseButtonEvent
import display.graphic.vertex.BasicShapes
import display.graphic.Color
import display.graphic.SnipRegion
import display.gui.*
import display.gui.base.GuiElementPhase.*
import display.gui.LayoutController.getOffsetForLayoutPosition
import display.gui.LayoutController.setElementsInRows
import display.gui.base.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.jbox2d.common.Vec2
import utility.Common.makeVec2
import utility.PidController
import utility.toSign

class GuiScroll(
    override val offset: Vec2 = Vec2(),
    override val scale: Vec2 = Vec2(100f, 100f),
    override var color: Color = Color.WHITE.setAlpha(.5f),
    override val kidElements: MutableList<GuiElement> = mutableListOf()
) : HasKids, HasScroll {

    override var id = GuiElementIdentifierType.DEFAULT
    override var currentPhase = IDLE
    override val updateCallback: (GuiElement) -> Unit = {}
    override var topRight = Vec2()
    override var bottomLeft = Vec2()
    override val onClick: () -> Unit = {}

    private lateinit var outline: FloatArray
    private lateinit var snipRegion: SnipRegion
    override val kidElementOffsets = HashMap<GuiElement, Vec2>()

    override var scrollBarPosition = 0f
    override var scrollBarPositionTarget = scrollBarPosition
    override val scrollController = PidController(-.06f, -.0001f, -.08f)
    override var scrollBarMax = 0f
    override var scrollBarMin = 0f

    private lateinit var scrollBarRegionScale: Vec2
    private lateinit var scrollBarRegionRelativeOffset: Vec2
    private lateinit var scrollBarRegionTopRight: Vec2
    private lateinit var scrollBarRegionBottomLeft: Vec2

    private lateinit var thumb: GuiIcon
    private lateinit var thumbScale: Vec2
    private lateinit var thumbRelativeOffset: Vec2
    private lateinit var thumbTopRight: Vec2
    private lateinit var thumbBottomLeft: Vec2

    private val availableScale
        get() = scale.sub(Vec2(thumbScale.x, 0f))

    private var lastMouseLocation = offset.sub(scale).sub(makeVec2(1))

    init {
        init()
    }

    private fun init() {
        val outlinePoints = BasicShapes.square
            .flatMap { (x, y) -> listOf(x * scale.x, y * scale.y) }
        outline = Drawer.getLine(outlinePoints, color, startWidth = 1f, wrapAround = true)

        thumbScale = Vec2(6f, scale.y * .5f)
        thumbRelativeOffset = getOffsetForLayoutPosition(LayoutPosition.TOP_RIGHT, scale, thumbScale)
        thumb = GuiIcon(scale = thumbScale, color = color, texture = TextureEnum.white_pixel)

        scrollBarRegionScale = Vec2(thumbScale.x, scale.y * 2f)
        scrollBarRegionRelativeOffset = Vec2(scale.x - thumbScale.x, 0f)

        if (kidElementOffsets.isEmpty()) {
            kidElementOffsets.putAll(kidElements.map {
                Pair(it, it.offset.add(Vec2(-thumbScale.x * 2, 0f)))
            })
        } else {
            updateScrollBarRange()
        }
        calculateElementRegion()
        calculateThumbRegion()
        calculateSnipRegion()
    }

    override fun render(parentSnipRegion: SnipRegion?) {
        val unionSnipRegion = snipRegion.intersect(parentSnipRegion)!!

        dI.textures.getTexture(TextureEnum.white_pixel).bind()
        dI.renderer.drawStrip(outline, offset, useCamera = false, snipRegion = unionSnipRegion)

        super<HasKids>.render(unionSnipRegion)
        thumb.render(unionSnipRegion)
    }

    override fun update() {
        super<HasScroll>.update()
        super<HasKids>.update()
        super<HasKids>.handleHover(lastMouseLocation)
    }

    override fun updateScale(newScale: Vec2) {
        super<HasKids>.updateScale(newScale)
        init()
    }

    override fun handleHover(location: Vec2) {
        lastMouseLocation = location
        if (isHover(location)) {
            super<HasKids>.handleHover(location)
        }
    }

    override fun scrollBarOnMove() {
        calculateThumbRegion()
        calculateNewOffsets()
        super.scrollBarOnMove()
    }

    override fun handleLeftClick(startEvent: MouseButtonEvent, event: Observable<MouseButtonEvent>): Boolean {
        return when {
            isThumbRegion(startEvent.location) -> {
                currentPhase = ACTIVE

                val distanceCalculator = DistanceCalculator()
                event.doOnNext {
                    val movement = distanceCalculator.getLastDistance(it.location)
                    addScrollBarPosition(-(movement.y / scale.y) * scrollBarMin)
                    calculateNewOffsets()
                }
                    .doOnComplete { currentPhase = IDLE }
                    .subscribe()
                true
            }
            isScrollbarRegion(startEvent.location) -> {
                val isAbove = startEvent.location.y > thumb.offset.y
                addScrollBarPosition(isAbove.toSign() * scale.y * 1.8f * 3f)
                true
            }
            isHover(startEvent.location) -> {
                currentPhase = DRAG

                val movementEvent = event.filter { !it.isPress && !it.isRelease }
                    .skip(1)

                val distanceCalculator = DistanceCalculator()
                movementEvent.doOnComplete { currentPhase = IDLE }
                    .subscribe {
                        val movement = distanceCalculator.getLastDistance(it.location)
                        addScrollBarPosition(-movement.y)
                        calculateNewOffsets()
                    }

                val unsubscribeKid = PublishSubject.create<Boolean>()
                movementEvent.take(1).subscribe { unsubscribeKid.onNext(true) }

                calculateNewOffsets()
                kidElements.filterIsInstance<HasClick>().any {
                    it.handleLeftClick(startEvent, event.takeUntil(unsubscribeKid))
                }

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
            .coerceAtMost(0f)
    }

    private fun calculateSnipRegion() {
        snipRegion = SnipRegion.create(this)
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
        kids.filter { it.scale.x > availableScale.x }
            .forEach {
                it.updateScale(Vec2(availableScale.x, it.scale.y))
                it.placeOnEdge(LayoutPosition.CENTER_LEFT, scale, it.scale)
            }

        kidElements.addAll(kids)
        setElementsInRows(kidElements, centered = false)

        kidElementOffsets.putAll(kids.map { Pair(it, it.offset.clone()) })
        calculateNewOffsets()
        updateScrollBarRange()
        return this
    }

}
