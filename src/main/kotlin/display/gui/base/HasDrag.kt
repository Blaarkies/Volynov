package display.gui.base

import display.gui.base.HasClick
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

    fun isDragRegion(location: Vec2): Boolean =
        isInRegion(location, dragHandleBottomLeft, dragHandleTopRight)

    fun calculateDraggableRegion() {
        dragHandleOffset = offset.add(dragHandleRelativeOffset)
        dragHandleTopRight = dragHandleOffset.add(dragHandleScale)
        dragHandleBottomLeft = dragHandleOffset.sub(dragHandleScale)
    }
}
