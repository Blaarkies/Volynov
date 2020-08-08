package display.gui.base

import display.events.MouseButtonEvent
import display.graphic.SnipRegion
import io.reactivex.Observable
import org.jbox2d.common.Vec2

interface HasElements : HasHover {

    val localElements: MutableList<GuiElement>
    val localElementOffsets: HashMap<GuiElement, Vec2>

    override fun render(parentSnipRegion: SnipRegion?) {
        super.render(parentSnipRegion)
        localElements.forEach { it.render(parentSnipRegion) }
    }

    override fun update() {
        super.update()
        localElements.forEach { it.update() }
    }

    override fun calculateElementRegion() {
        super.calculateElementRegion()
        localElements.forEach { it.updateOffset(localElementOffsets[it]!!.add(offset)) }
    }

    override fun updateScale(newScale: Vec2) {
        val scaleRatio = Vec2(newScale.x / scale.x, newScale.y / scale.y)
        localElementOffsets.forEach {
            val scale = it.value
            scale.set(scale.x * scaleRatio.x, scale.y * scaleRatio.y)
        }
        super.updateScale(newScale)
    }

    override fun handleHover(location: Vec2) {
        super.handleHover(location)
        localElements.filterIsInstance<HasHover>().forEach { it.handleHover(location) }
    }

    fun handleLeftClick(startEvent: MouseButtonEvent, event: Observable<MouseButtonEvent>): Boolean {
        val isHovered = isHover(startEvent.location)
        if (isHovered) {
            val localTakeEvent = localElements.filterIsInstance<HasClick>()
                .any { it.handleLeftClick(startEvent, event) }
            if (localTakeEvent) return true
        }
        return isHovered
    }

    fun calculateNewScale() {
        val bottomLeft = localElements
            .map { it.offset.sub(it.scale) }
            .let { bl -> Vec2(bl.minBy { it.x }!!.x, bl.minBy { it.y }!!.y) }

        val topRight = localElements
            .map { it.offset.add(it.scale) }
            .let { tr -> Vec2(tr.maxBy { it.x }!!.x, tr.maxBy { it.y }!!.y) }

        val newScale = topRight.sub(bottomLeft).mul(.5f)
        if (newScale.sub(scale).length() == 0f) return
        updateScale(newScale)
    }

}
