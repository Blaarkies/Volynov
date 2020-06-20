package display.gui

import org.jbox2d.common.Vec2

interface HasKids : HasClick {

    val kidElements: MutableList<GuiElement>
    val kidElementOffsets: HashMap<GuiElement, Vec2>

    override fun update() {
        kidElements.forEach { it.update() }
        super.update()
    }

    override fun addOffset(movement: Vec2) {
        super.addOffset(movement)
        calculateNewOffsets()
    }

    override fun updateOffset(newOffset: Vec2) {
        super.updateOffset(newOffset)
        calculateNewOffsets()
    }

    override fun handleHover(location: Vec2): Boolean {
        super.handleHover(location)
        kidElements.filterIsInstance<HasHover>()
            .forEach { it.handleHover(location) }
        return false
    }

    override fun handleLeftClickPress(location: Vec2): Boolean =
        super.handleHover(location) and
                kidElements.filterIsInstance<HasClick>()
                    .any { it.handleLeftClickPress(location) }

    override fun handleLeftClickRelease(location: Vec2): Boolean =
        super.handleLeftClickRelease(location) or
                kidElements.filterIsInstance<HasClick>()
                    .any { it.handleLeftClickRelease(location) }

    fun handleScroll(location: Vec2, movement: Vec2): Boolean =
        kidElements.filterIsInstance<HasScroll>()
            .any { it.handleScroll(location, movement) }

    fun handleLeftClickDrag(location: Vec2, movement: Vec2): Boolean =
        kidElements.filterIsInstance<HasScroll>()
            .any { it.handleLeftClickDrag(location, movement) }

    fun addKids(kids: List<GuiElement>): HasKids {
        kidElements.addAll(kids)
        kidElementOffsets.putAll(kids.map { Pair(it, it.offset.clone()) })
        calculateNewOffsets()
        return this
    }

    fun addKid(kid: GuiElement): HasKids = addKids(listOf(kid))

    fun calculateNewOffsets() {
        kidElements.forEach { it.updateOffset(kidElementOffsets[it]!!.add(offset)) }
    }
}
