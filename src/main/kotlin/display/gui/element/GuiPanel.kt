package display.gui.element

import dI
import display.draw.Drawer
import display.draw.TextureEnum
import display.event.DistanceCalculator
import display.event.MouseButtonEvent
import display.graphic.vertex.BasicShapes
import display.graphic.Color
import display.graphic.SnipRegion
import display.gui.base.GuiElementPhase.*
import display.gui.base.*
import display.text.TextJustify
import io.reactivex.Observable
import org.jbox2d.common.Vec2
import utility.Common.makeVec2
import utility.Common.vectorUnit

open class GuiPanel(
    override val offset: Vec2 = Vec2(),
    override val scale: Vec2 = Vec2(100f, 100f),
    var title: String = "",
    override var color: Color = Color.BLACK.setAlpha(.5f),
    override val kidElements: MutableList<GuiElement> = mutableListOf(),
    override val draggable: Boolean = true,
    override val updateCallback: (GuiElement) -> Unit = {}
) : HasKids, HasDrag {

    override var id = GuiElementIdentifierType.DEFAULT
    override var currentPhase = IDLE
    override var topRight = Vec2()
    override var bottomLeft = Vec2()
    override val onClick: () -> Unit = {}

    private val draggableOutline: FloatArray
    private val background: FloatArray

    override val kidElementOffsets = HashMap<GuiElement, Vec2>()

    override val dragHandleScale: Vec2
    override lateinit var dragHandleRelativeOffset: Vec2
    override lateinit var dragHandleOffset: Vec2
    override lateinit var dragHandleTopRight: Vec2
    override lateinit var dragHandleBottomLeft: Vec2

    init {
        background = BasicShapes.square
            .let { Drawer.getColoredData(it, color) }
            .toFloatArray()

        dragHandleScale = Vec2(90f, 25f)
        dragHandleRelativeOffset = Vec2(0f, scale.y - dragHandleScale.y)
        val linePoints = BasicShapes.square.chunked(2)
            .flatMap { (x, y) -> listOf(x * dragHandleScale.x, y * dragHandleScale.y) }
        draggableOutline = Drawer.getLine(linePoints, Color.WHITE.setAlpha(.3f), startWidth = .5f, wrapAround = true)

        if (draggable) {
            addKids(listOf(-1f, 1f).map {
                GuiIcon(dragHandleRelativeOffset.add(Vec2(it * (dragHandleScale.x - 20), 0f)),
                    makeVec2(20), Color.WHITE.setAlpha(.5f),
                    texture = TextureEnum.icon_draggable, padding = makeVec2(10))
            })
        }

        kidElementOffsets.putAll(kidElements.map { Pair(it, it.offset.clone()) })
        calculateElementRegion()
        calculateDraggableRegion()
    }

    override fun render(parentSnipRegion: SnipRegion?) {
        dI.textures.getTexture(TextureEnum.white_pixel).bind()
        dI.renderer.drawShape(background, offset, 0f, scale, useCamera = false, snipRegion = parentSnipRegion)

        if (draggable) {
            dI.renderer.drawStrip(draggableOutline, dragHandleOffset, useCamera = false, snipRegion = parentSnipRegion)
        }

        dI.renderer.drawText(
            title,
            offset.add(Vec2(0f, scale.y - 25f)),
            vectorUnit.mul(.15f),
            Color.WHITE.setAlpha(.7f),
            TextJustify.CENTER,
            false,
            parentSnipRegion
        )

        super<HasKids>.render(parentSnipRegion)
    }

    override fun addOffset(movement: Vec2) {
        if (movement.length() == 0f) return
        super<HasKids>.addOffset(movement)
        super.calculateDraggableRegion()
    }

    override fun updateOffset(newOffset: Vec2) {
        super<HasKids>.updateOffset(newOffset)
        super.calculateDraggableRegion()
    }

    override fun handleLeftClick(startEvent: MouseButtonEvent, event: Observable<MouseButtonEvent>): Boolean {
        val isHovered = isHover(startEvent.location)
        if (isHovered) {
            val kidTakesEvent = kidElements.filterIsInstance<HasClick>()
                .any { it.handleLeftClick(startEvent, event) }
                    || kidElements.filterIsInstance<HasElements>()
                .any { it.handleLeftClick(startEvent, event) }

            if (!kidTakesEvent
                && isDragRegion(startEvent.location)
                && draggable) {
                currentPhase = ACTIVE

                val distanceCalculator = DistanceCalculator()
                event.doOnComplete { currentPhase = IDLE }
                    .subscribe {
                        val movement = distanceCalculator.getLastDistance(it.location)
                        addOffset(movement)
                    }
                return true
            }
        }
        return isHovered
    }

}
