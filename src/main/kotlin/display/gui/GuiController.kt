package display.gui

import display.draw.Drawer
import org.jbox2d.common.Vec2

class GuiController(private val drawer: Drawer) {

    private val elements = mutableListOf<GuiElement>()

    fun render() {
        elements.forEach { it.render() }
    }

    fun checkHover(location: Vec2) {
        elements.filterIsInstance<GuiButton>().forEach { it.handleHover(location) }
    }

    fun checkLeftClick(location: Vec2) {
        elements.filterIsInstance<GuiButton>().forEach { it.handleClick(location) }
    }

    fun createMainMenu(
        onClickNewGame: () -> Unit,
        onClickSettings: () -> Unit,
        onClickQuit: () -> Unit
    ) {
        elements.addAll(
            listOf(
                GuiLabel(drawer, "Volynov", Vec2(-10f, 250f), .6f),
                GuiButton(drawer, "New Game", offset = Vec2(0f, 100f), onClick = onClickNewGame),
                GuiButton(drawer, "Settings", offset = Vec2(0f, -20f), onClick = onClickSettings),
                GuiButton(drawer, "Quit", offset = Vec2(0f, -140f), onClick = onClickQuit)
            )
        )
    }


}
