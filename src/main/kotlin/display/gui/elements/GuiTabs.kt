package display.gui.elements

import display.events.DistanceCalculator
import display.events.MouseButtonEvent
import display.graphic.Color
import display.graphic.SnipRegion
import display.gui.LayoutController
import display.gui.LayoutPosition
import display.gui.base.*
import display.gui.base.GuiElementPhases.DRAG
import display.gui.base.GuiElementPhases.IDLE
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.jbox2d.common.Vec2
import utility.Common
import utility.PidController
import kotlin.math.absoluteValue

class GuiTabs(
    override val offset: Vec2 = Vec2(),
    override val scale: Vec2 = Vec2(100f, 100f),
    override val color: Color = Color.WHITE.setAlpha(.5f),
    override val kidElements: MutableList<GuiElement> = mutableListOf(),
    override val updateCallback: (GuiElement) -> Unit = {}
) : HasKids {

    override var id = GuiElementIdentifierType.DEFAULT
    override var currentPhase = IDLE
    override var topRight = Vec2()
    override var bottomLeft = Vec2()
    override val onClick: () -> Unit = {}

    private lateinit var snipRegion: SnipRegion
    override val kidElementOffsets = HashMap<GuiElement, Vec2>()

    private var pagesOffset = 0f
    private var pagesOffsetTarget = pagesOffset
    private val pagesOffsetController = PidController(-.12f, -.0001f, -.1f)
    private var pagesOffsetMin = 0f
    private var pagesOffsetMax = 0f

    private val localElements = mutableListOf<GuiElement>()
    private val localElementOffsets = HashMap<GuiElement, Vec2>()
    private val tabsScale = Vec2(scale.x, 20f)

    private var lastMouseLocation = offset.sub(scale).sub(Common.makeVec2(1))

    init {
        kidElementOffsets.putAll(kidElements.map { Pair(it, it.offset.clone()) })
        calculateElementRegion()
        calculateSnipRegion()

        addTabButtons()
    }

    private fun addTabButtons() {
        val tabCount = kidElements.size
        val tabSize = scale.x / tabCount
        localElements.clear()
        localElements.addAll(kidElements
            .zip(listOf("Weapons", "Shields", "Fuels"))
            .withIndex().map { (index, page) ->
                val (element, title) = page
                val tabOffset = Vec2(-scale.x + tabSize, scale.y)
                    .addLocal(index * scale.x * 2 / tabCount, -20f)
                GuiButton(tabOffset, Vec2(tabSize, 20f), title, .1f,
                    onClick = { pagesOffsetTarget = -kidElementOffsets[element]!!.x })
            })
        localElementOffsets.putAll(localElements.map { Pair(it, it.offset.clone()) })
    }

    override fun render(parentSnipRegion: SnipRegion?) {
        val unionSnipRegion = snipRegion.intersect(parentSnipRegion)
        super.render(unionSnipRegion)

        localElements.forEach { it.render(parentSnipRegion) }
    }

    override fun update() {
        if (pagesOffset.minus(pagesOffsetTarget).absoluteValue > .01f) {
            val movement = pagesOffsetController.getReaction(pagesOffset, pagesOffsetTarget)
            pagesOffset = (pagesOffset + movement).coerceIn(pagesOffsetMin, pagesOffsetMax)
            calculateNewOffsets()
        }
        super.update()
        super.handleHover(lastMouseLocation)
    }

    override fun handleHover(location: Vec2) {
        lastMouseLocation = location
        if (isHover(location)) {
            super.handleHover(location)
            localElements.filterIsInstance<HasHover>().forEach { it.handleHover(location) }
        }
    }

    override fun handleLeftClick(startEvent: MouseButtonEvent, event: Observable<MouseButtonEvent>): Boolean {
        val isHovered = isHover(startEvent.location)
        if (isHovered) {
            val tabsTakeEvent = localElements.filterIsInstance<HasClick>()
                .any { it.handleLeftClick(startEvent, event) }
            if (tabsTakeEvent) return true

            currentPhase = DRAG

            val movementEvent = event.filter { !it.isPress && !it.isRelease }
                .skipWhile { startEvent.location.sub(it.location).x.absoluteValue <= scale.x * .5f }

            val distanceCalculator = DistanceCalculator()
            movementEvent.doOnComplete { currentPhase = IDLE }
                .map { distanceCalculator.getLastDistance(it.location) }
                .subscribe { addPagesOffset(it) }
            movementEvent.lastElement()
                .subscribe {
                    pagesOffsetTarget = kidElementOffsets.map { Pair(it, it.value.x + pagesOffsetTarget) }
                        .minBy { (_, distance) -> distance.absoluteValue }!!
                        .let { (kv, _) -> -kv.value.x }
                }

            val unsubscribeKid = PublishSubject.create<Boolean>()
            movementEvent.take(1).subscribe { unsubscribeKid.onNext(true) }

            kidElements.filterIsInstance<HasClick>().any {
                it.handleLeftClick(startEvent, event.takeUntil(unsubscribeKid))
            }
        }
        return isHovered
    }

    private fun addPagesOffset(movement: Vec2) {
        pagesOffsetTarget = (pagesOffsetTarget + movement.x).coerceIn(pagesOffsetMin, pagesOffsetMax)
        calculateNewOffsets()
    }

    private fun calculateSnipRegion() {
        snipRegion = SnipRegion.create(this)
    }

    override fun calculateNewOffsets() {
        calculateSnipRegion()

        kidElements.forEach {
            it.updateOffset(kidElementOffsets[it]!!
                .add(Vec2(pagesOffset, 0f))
                .add(offset))
        }

        localElements.forEach { it.updateOffset(localElementOffsets[it]!!.add(offset)) }
    }

    override fun addKids(kids: List<GuiElement>): GuiTabs {
        val pageScale = scale.sub(Vec2(0f, tabsScale.y * 2))
        kids.forEach {
            it.updateOffset(LayoutController.getOffsetForLayoutPosition(
                LayoutPosition.TOP_LEFT, pageScale, it.scale))
        }

        kidElements.addAll(kids)
        LayoutController.setElementsInColumns(kidElements, centered = false)

        kidElementOffsets.putAll(kids.map {
            Pair(it, it.offset.clone())
        })
        addTabButtons()

        pagesOffsetMin = kids.last().let { -it.offset.x }
        calculateNewOffsets()
        return this
    }

}