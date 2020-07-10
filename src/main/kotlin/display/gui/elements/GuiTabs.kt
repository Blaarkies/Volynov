package display.gui.elements

import display.events.DistanceCalculator
import display.events.MouseButtonEvent
import display.graphic.Color
import display.graphic.SnipRegion
import display.gui.LayoutController
import display.gui.base.GuiElementPhases.*
import display.gui.base.*
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

    private var lastMouseLocation = offset.sub(scale).sub(Common.makeVec2(1))

    init {
        kidElementOffsets.putAll(kidElements.map { Pair(it, it.offset.clone()) })
        calculateElementRegion()
        calculateSnipRegion()
    }

    override fun render(parentSnipRegion: SnipRegion?) {
        kidElements.filter {
            it.offset.sub(it.scale).y < offset.add(scale).y
                    && it.offset.add(it.scale).y > offset.sub(scale).y
        }.forEach { it.render(snipRegion) }

//        super.render(snipRegion)
    }

    override fun update() {
        if (pagesOffset.minus(pagesOffsetTarget).absoluteValue > .1f) {
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
        }
    }

    override fun handleLeftClick(startEvent: MouseButtonEvent, event: Observable<MouseButtonEvent>): Boolean {
        val isHovered = isHover(startEvent.location)
        if (isHovered) {
            currentPhase = DRAG

            val movementEvent = event.filter { !it.isPress && !it.isRelease }
                .skipWhile { startEvent.location.sub(it.location).x.absoluteValue <= scale.x * .5f }

            val distanceCalculator = DistanceCalculator()
            movementEvent.doOnComplete { currentPhase = IDLE }
                .map { distanceCalculator.getLastDistance(it.location) }
                .subscribe {
                    addPagesOffset(it)
                }
            movementEvent.lastElement()
                .subscribe {
                    pagesOffsetTarget = kidElementOffsets.map { Pair(it, it.value.x + pagesOffsetTarget) }
                        .minBy { (_, distance) -> distance.absoluteValue }!!
                        .let { (kv, _) -> -kv.value.x }
                }

            val unsubscribeKid = PublishSubject.create<Boolean>()
            movementEvent.take(1).subscribe { unsubscribeKid.onNext(true) }

            calculateNewOffsets()
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
        snipRegion = SnipRegion(offset.sub(scale), scale.mul(2f))
    }

    override fun calculateNewOffsets() {
        calculateSnipRegion()

        kidElements.forEach {
            it.updateOffset(kidElementOffsets[it]!!
                .add(Vec2(pagesOffset, 0f))
                .add(offset))
        }
    }

    override fun addKids(kids: List<GuiElement>): GuiTabs {
        kidElements.addAll(kids)
        LayoutController.setElementsInColumns(kidElements, centered = false)

        kidElementOffsets.putAll(kids.map {
            Pair(it, it.offset.clone())
        })
        pagesOffsetMin = kids.last().let { -it.offset.x }
        calculateNewOffsets()
        return this
    }

}
