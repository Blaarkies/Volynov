package display.gui

import display.draw.Drawer
import display.draw.TextureEnum
import display.graphic.BasicShapes
import display.graphic.Color
import display.graphic.SnipRegion
import display.text.TextJustify
import org.jbox2d.common.Vec2
import utility.Common
import utility.Common.makeVec2

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

    private val draggableOutline: FloatArray
    private val background: FloatArray
    private val childElementOffsets = HashMap<GuiElement, Vec2>()

    private var isPressed = false
    private val dragHandleScale: Vec2
    private var dragHandleRelativeOffset: Vec2

    private lateinit var dragHandleOffset: Vec2
    private lateinit var dragHandleTopRight: Vec2
    private lateinit var dragHandleBottomLeft: Vec2

    init {
        background = BasicShapes.square
            .let { Drawer.getColoredData(it, color) }
            .toFloatArray()

        dragHandleScale = Vec2(90f, 25f)
        dragHandleRelativeOffset = Vec2(0f, scale.y - dragHandleScale.y)
        val linePoints = BasicShapes.square
            .chunked(2)
            .flatMap { (x, y) -> listOf(x * dragHandleScale.x, y * dragHandleScale.y) }
        draggableOutline = Drawer.getLine(linePoints, Color.WHITE.setAlpha(.3f), startWidth = 1f, wrapAround = true)

        if (draggable) {
            addChildren(listOf(-1f, 1f).map {
                GuiIcon(drawer, dragHandleRelativeOffset.add(Vec2(it * (dragHandleScale.x - 20), 0f)),
                    makeVec2(6), color = Color.WHITE.setAlpha(.5f), texture = TextureEnum.icon_draggable)
            })
        }

        childElementOffsets.putAll(childElements.map { Pair(it, it.offset.clone()) })
        calculateElementRegion(this)
        calculateDraggableRegion()
    }

    override fun render(snipRegion: SnipRegion?) {
        drawer.textures.getTexture(TextureEnum.white_pixel).bind()
        drawer.renderer.drawShape(background, offset, 0f, scale, useCamera = false, snipRegion = snipRegion)

        if (draggable) {
            drawer.renderer.drawStrip(draggableOutline, dragHandleOffset, useCamera = false, snipRegion = snipRegion)
        }

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
        super.addOffset(newOffset)
        calculateNewOffsets()
        calculateDraggableRegion()
    }

    override fun updateOffset(newOffset: Vec2) {
        super.updateOffset(newOffset)
        calculateNewOffsets()
        calculateDraggableRegion()
    }

    override fun handleHover(location: Vec2) {
        if (isHover(location)) {
            childElements.forEach { it.handleHover(location) }
        }
    }

    override fun handleLeftClickPress(location: Vec2): Boolean {
        return isHover(location)
                && childElements.any { it.handleLeftClickPress(location) }
            .or(didParentPress(location))
    }

    private fun didParentPress(location: Vec2): Boolean {
        return if (isDragRegion(location)) {
            isPressed = true
            true
        } else false
    }

    override fun handleLeftClickRelease(location: Vec2): Boolean {
        isPressed = false
        return childElements.any { it.handleLeftClickRelease(location) }
    }

    override fun handleLeftClickDrag(location: Vec2, movement: Vec2): Boolean {
        val kidDragged = childElements.any { it.handleLeftClickDrag(location, movement) }
        return kidDragged || didParentDrag(location, movement)
    }

    private fun didParentDrag(location: Vec2, movement: Vec2): Boolean {
        return if (draggable && isPressed && isDragRegion(location)) {
            addOffset(movement)
            true
        } else false
    }

    private fun isDragRegion(location: Vec2): Boolean =
        isInRegion(location, dragHandleBottomLeft, dragHandleTopRight)

    override fun handleScroll(location: Vec2, movement: Vec2): Boolean {
        return isHover(location)
                && childElements.any { it.handleScroll(location, movement) }
    }

    private fun calculateNewOffsets() {
        childElements.forEach { it.updateOffset(childElementOffsets[it]!!.add(offset)) }
    }

    private fun calculateDraggableRegion() {
        dragHandleOffset = offset.add(dragHandleRelativeOffset)

        dragHandleTopRight = dragHandleOffset.add(dragHandleScale)
        dragHandleBottomLeft = dragHandleOffset.sub(dragHandleScale)
    }

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
