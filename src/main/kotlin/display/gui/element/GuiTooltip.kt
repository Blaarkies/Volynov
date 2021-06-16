package display.gui.element

import dI
import display.draw.TextureEnum
import display.graphic.Color
import display.graphic.SnipRegion
import display.gui.LayoutController
import display.gui.base.GuiElement
import display.gui.base.GuiElementIdentifierType
import display.gui.base.GuiElementPhase.IDLE
import display.gui.base.HasOutline
import io.reactivex.Observable.just
import io.reactivex.subjects.PublishSubject
import org.jbox2d.common.Vec2
import utility.Common.makeVec2
import java.util.concurrent.TimeUnit

class GuiTooltip(
    override val offset: Vec2 = Vec2(),
    override val scale: Vec2 = Vec2(),
    override val updateCallback: (GuiElement) -> Unit = {}
) : HasOutline {

    override lateinit var outline: FloatArray
    override lateinit var activeBackground: FloatArray
    override var color: Color = Color.WHITE.setAlpha(.5f)
    override var backgroundColor = Color.BLACK.setAlpha(.9f)

    override var id = GuiElementIdentifierType.DEFAULT
    override var currentPhase = IDLE

    private lateinit var snipRegion: SnipRegion
    private val localElements = mutableListOf<GuiElement>()
    private val localElementOffsets = HashMap<GuiElement, Vec2>()

    private val unsubscribe = PublishSubject.create<Boolean>()

    init {
        dI.whenDone
            .switchMap { dI.inputHandler.mouseButtonEvent }
            .filter { it.isPress && localElements.size > 0 }
            .takeUntil(unsubscribe)
            .subscribe { handleLeftClick() }
    }

    fun dispose() {
        unsubscribe.onNext(true)
    }

    override fun render(parentSnipRegion: SnipRegion?) {
        if (localElements.size == 0) return

        dI.textures.getTexture(TextureEnum.white_pixel).bind()
        dI.oldRenderer.drawShape(activeBackground, offset, useCamera = false, snipRegion = snipRegion)

        localElements.forEach { it.render(snipRegion) }
        super.render(snipRegion)
    }

    private fun handleLeftClick() {
        localElements.clear()
    }

    private fun calculateSnipRegion() {
        snipRegion = SnipRegion.create(this)
    }

    override fun updateScale(newScale: Vec2) {
        super.updateScale(newScale)
        calculateVisuals()
    }

    private fun calculateNewScale() {
        val bottomLeft = localElements
            .map { it.offset.sub(it.scale) }
            .let { bl -> Vec2(bl.minByOrNull { it.x }!!.x, bl.minByOrNull { it.y }!!.y) }

        val topRight = localElements
            .map { it.offset.add(it.scale) }
            .let { tr -> Vec2(tr.maxByOrNull { it.x }!!.x, tr.maxByOrNull { it.y }!!.y) }

        updateScale(topRight.sub(bottomLeft).mul(.5f))
    }

    private fun calculateNewOffsets() {
        calculateSnipRegion()
        localElements.forEach { it.updateOffset(localElementOffsets[it]!!.add(offset)) }
    }

    fun showElement(kid: GuiElement, atOffset: Vec2) {
        val screenScale = Vec2(dI.cameraView.windowWidth, dI.cameraView.windowHeight).mul(.5f)

        val padding = makeVec2(20)
        val moveIntoScreenOffset = LayoutController.getOffsetToFitScaleInside(
            screenScale.sub(padding), Vec2(), kid.scale, atOffset)

        just(0).delay(32, TimeUnit.MILLISECONDS).subscribe {
            updateOffset(atOffset.add(moveIntoScreenOffset))
            addKid(kid)
        }
    }

    fun addKid(kid: GuiElement): GuiTooltip = addKids(listOf(kid))

    fun addKids(kids: List<GuiElement>): GuiTooltip {
        localElements.clear()

        localElements.addAll(kids)
        localElementOffsets.putAll(localElements.map { Pair(it, it.offset.clone()) })

        calculateNewScale()
        calculateNewOffsets()
        return this
    }

    fun clear() {
        localElements.clear()
    }

}
