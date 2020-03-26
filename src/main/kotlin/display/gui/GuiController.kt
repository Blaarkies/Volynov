package display.gui

import display.draw.Drawer
import game.GamePlayer
import org.jbox2d.common.Vec2

class GuiController(private val drawer: Drawer) {

    private val elements = mutableListOf<GuiElement>()

    fun render() = elements.forEach { it.render() }

    fun clear() = elements.clear()

    fun checkHover(location: Vec2) = elements.filterIsInstance<GuiButton>().forEach { it.handleHover(location) }

    fun checkLeftClick(location: Vec2) = elements.filterIsInstance<GuiButton>().forEach { it.handleClick(location) }

    fun createMainMenu(
        onClickNewGame: () -> Unit,
        onClickSettings: () -> Unit,
        onClickQuit: () -> Unit
    ) {
        val menuButtons = listOf(
            GuiButton(drawer, title = "New Game", textSize = .3f, onClick = onClickNewGame),
            GuiButton(drawer, title = "Settings", textSize = .3f, onClick = onClickSettings),
            GuiButton(drawer, title = "Quit", textSize = .3f, onClick = onClickQuit)
        )
        setElementsInRows(menuButtons, 40f, false)
        menuButtons.forEach { it.setOffset(Vec2(0f, 150f)) }

        elements.add(GuiLabel(drawer, Vec2(-10f, 250f), "Volynov", .6f))
        elements.addAll(menuButtons)
    }


    fun createMainMenuSelectPlayers(
        onClickStart: () -> Unit,
        onClickCancel: () -> Unit,
        onAddPlayer: () -> Unit,
        onRemovePlayer: () -> Unit,
        playerList: MutableList<GamePlayer>
    ) {
        elements.add(GuiLabel(drawer, Vec2(-10f, 250f), "Select Players", .2f))

        updateMainMenuSelectPlayers(playerList, onAddPlayer, onRemovePlayer)

        val actionButtonSize = Vec2(100f, 25f)
        val actionButtons = listOf(
            GuiButton(drawer, scale = actionButtonSize, title = "Cancel", textSize = .15f, onClick = onClickCancel),
            GuiButton(drawer, scale = actionButtonSize, title = "Start", textSize = .15f, onClick = onClickStart)
        )
        setElementsInColumns(actionButtons, 40f)
        actionButtons.forEach { it.setOffset(Vec2(0f, -150f)) }
        elements.addAll(actionButtons)
    }

    fun updateMainMenuSelectPlayers(
        players: MutableList<GamePlayer>,
        onAddPlayer: () -> Unit,
        onRemovePlayer: () -> Unit
    ) {
        val indexOfPlayersButtons = elements.indexOfFirst { it.id == GuiElementIdentifierType.ADD_PLAYERS_GROUP }
        elements.removeIf { it.id == GuiElementIdentifierType.ADD_PLAYERS_GROUP }

        val playerButtonSize = Vec2(50f, 50f)
        val playerButtons = players
            .map { GuiButton(drawer, scale = playerButtonSize, title = "P${it.name}", textSize = .3f) }
            .let {
                it + listOf(
                    GuiButton(
                        drawer, scale = playerButtonSize, title = " + ", textSize = .3f,
                        onClick = if (it.size < 4) onAddPlayer else {
                            {}
                        }
                    )
                )
            }
        setElementsInColumns(playerButtons, 40f)
        when (indexOfPlayersButtons) {
            -1 -> elements.addAll(playerButtons)
            else -> elements.addAll(indexOfPlayersButtons, playerButtons)
        }

        playerButtons.forEach { it.id = GuiElementIdentifierType.ADD_PLAYERS_GROUP }
    }

    private fun setElementsInColumns(elements: List<GuiElement>, gap: Float, centered: Boolean = true) {
        val totalWidth = elements.map { it.scale.x * 2f }.sum() + gap * (elements.size - 1)
        val columnSize = totalWidth / elements.size

        elements.withIndex()
            .forEach { (index, element) ->
                val newXOffset = when (centered) {
                    true -> columnSize * (index - (elements.size - 1) * .5f)
                    else -> columnSize * index + columnSize * .5f
                }
                element.setOffset(Vec2(newXOffset, element.offset.y))
            }
    }

    private fun setElementsInRows(elements: List<GuiElement>, gap: Float, centered: Boolean = true) {
        val totalHeight = elements.map { it.scale.y * 2f }.sum() + gap * (elements.size - 1)
        val rowSize = totalHeight / elements.size

        elements.withIndex()
            .forEach { (index, element) ->
                val newYOffset = when (centered) {
                    true -> rowSize * (index - (elements.size - 1) * .5f)
                    else -> rowSize * index + rowSize * .5f
                } * -1f
                element.setOffset(Vec2(element.offset.x, newYOffset))
            }
    }

}
