package display.gui.base

import display.graphic.SnipRegion
import org.jbox2d.common.Vec2

interface HasKids : HasClick {

    val kidElements: MutableList<GuiElement>
    val kidElementOffsets: HashMap<GuiElement, Vec2>

    override fun render(parentSnipRegion: SnipRegion?) {
        super.render(parentSnipRegion)
        kidElements.filter { kid ->
            kid.offset.sub(kid.scale).y < offset.add(scale).y
                    && kid.offset.add(kid.scale).y > offset.sub(scale).y
                    && kid.offset.sub(kid.scale).x < offset.add(scale).x
                    && kid.offset.add(kid.scale).x > offset.sub(scale).x
        }.forEach { it.render(parentSnipRegion) }
    }

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

    override fun handleHover(location: Vec2) {
        super.handleHover(location)
        kidElements.filterIsInstance<HasHover>()
            .forEach { it.handleHover(location) }
    }

    fun handleScroll(location: Vec2, movement: Vec2): Boolean {
        return if (isHover(location)) {
            kidElements.filterIsInstance<HasKids>().any { it.handleScroll(location, movement) }
                    || kidElements.filterIsInstance<HasScroll>().any { it.handleScroll(location, movement) }
        } else false
    }

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

    fun getFlatListKidElements(): List<GuiElement> {
        return listOf(this)
            .union(kidElements.filter { it !is HasKids })
            .union(kidElements.filterIsInstance<HasKids>().flatMap { it.getFlatListKidElements() })
            .toList()
    }

}
