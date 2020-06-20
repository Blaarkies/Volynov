package display.gui

import org.jbox2d.common.Vec2

interface HasDrag : HasClick {

    val draggable: Boolean
    val dragHandleScale: Vec2
    var dragHandleRelativeOffset: Vec2
    var dragHandleOffset: Vec2
    var dragHandleTopRight: Vec2
    var dragHandleBottomLeft: Vec2

    override fun addOffset(movement: Vec2) {
        super.addOffset(movement)
        calculateDraggableRegion()
    }

    override fun updateOffset(newOffset: Vec2) {
        super.updateOffset(newOffset)
        calculateDraggableRegion()
    }

    override fun handleLeftClickPress(location: Vec2): Boolean =
        super.handleHover(location) or didParentPress(location)

    fun didParentPress(location: Vec2): Boolean {
        return if (isDragRegion(location)) {
            isPressed = true
            true
        } else false
    }

    fun handleLeftClickDrag(location: Vec2, movement: Vec2): Boolean =
        didParentDrag(location, movement)

    fun didParentDrag(location: Vec2, movement: Vec2): Boolean {
        return if (draggable && isPressed && isDragRegion(location)) {
            addOffset(movement)
            true
        } else false
    }

    private fun isDragRegion(location: Vec2): Boolean =
        isInRegion(location, dragHandleBottomLeft, dragHandleTopRight)

    fun calculateDraggableRegion() {
        dragHandleOffset = offset.add(dragHandleRelativeOffset)
        dragHandleTopRight = dragHandleOffset.add(dragHandleScale)
        dragHandleBottomLeft = dragHandleOffset.sub(dragHandleScale)
    }
}
