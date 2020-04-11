package display.gui

import display.draw.Drawer
import display.graphic.Color
import game.GamePlayer
import org.jbox2d.common.Vec2
import utility.Common.roundFloat
import kotlin.math.roundToInt

class GuiController(private val drawer: Drawer, val setTextInputState: () -> Unit) {

    private val elements = mutableListOf<GuiElement>()

    fun render() = elements.forEach { it.render() }

    fun update() = elements.forEach { it.update() }

    fun clear() = elements.clear()

    fun checkHover(location: Vec2) = elements.forEach { it.handleHover(location) }

    fun checkLeftClick(location: Vec2) = elements.toList().forEach { it.handleClick(location) }

    fun checkAddTextInput(text: String) = elements.filterIsInstance<GuiInput>().forEach { it.handleAddTextInput(text) }

    fun checkRemoveTextInput() = elements.filterIsInstance<GuiInput>().forEach { it.handleRemoveTextInput() }

    fun stopTextInput() = elements.filterIsInstance<GuiInput>().forEach { it.stopTextInput() }

    fun createMainMenu(
        onClickNewGame: () -> Unit,
        onClickSettings: () -> Unit,
        onClickQuit: () -> Unit
    ) {
        clear()
        val buttonScale = Vec2(200f, 44f)
        val menuButtons = listOf(
            GuiButton(drawer, scale = buttonScale, title = "New Game", textSize = .27f, onClick = onClickNewGame),
            GuiButton(drawer, scale = buttonScale, title = "Settings", textSize = .27f, onClick = onClickSettings),
            GuiButton(drawer, scale = buttonScale, title = "Credits", textSize = .27f, onClick = {}),
            GuiButton(drawer, scale = buttonScale, title = "Quit", textSize = .27f, onClick = onClickQuit)
        )
        setElementsInRows(menuButtons, 40f, false)
        menuButtons.forEach { it.addOffset(Vec2(0f, 150f)) }

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
        clear()
        elements.add(GuiLabel(drawer, Vec2(-10f, 250f), "Select Players", .2f))

        updateMainMenuSelectPlayers(playerList, onAddPlayer, onRemovePlayer)

        val actionButtonSize = Vec2(100f, 25f)
        val actionButtons = listOf(
            GuiButton(drawer, scale = actionButtonSize, title = "Cancel", textSize = .15f, onClick = onClickCancel),
            GuiButton(drawer, scale = actionButtonSize, title = "Start", textSize = .15f, onClick = onClickStart)
        )
        setElementsInColumns(actionButtons, 40f)
        actionButtons.forEach { it.addOffset(Vec2(0f, -150f)) }
        elements.addAll(actionButtons)
    }

    fun updateMainMenuSelectPlayers(
        players: MutableList<GamePlayer>,
        onAddPlayer: () -> Unit,
        onRemovePlayer: () -> Unit
    ) {
        val indexOfPlayersButtons = elements.indexOfFirst { it.id == GuiElementIdentifierType.ADD_PLAYERS_GROUP }
        elements.removeIf { it.id == GuiElementIdentifierType.ADD_PLAYERS_GROUP }

        val noOpCallback = {}

        val playerButtonSize = Vec2(50f, 50f)
        val playerButtonHalfSize = playerButtonSize.clone()
        playerButtonHalfSize.y = playerButtonSize.y * .5f

        val addPlayerButton = GuiButton(
            drawer, scale = playerButtonHalfSize, title = " + ", textSize = .3f,
            onClick = if (players.size < 4) onAddPlayer else noOpCallback
        )
        val removePlayerButton = GuiButton(
            drawer, scale = playerButtonHalfSize, title = " - ", textSize = .3f,
            onClick = if (players.size > 0) onRemovePlayer else noOpCallback
        )
        val addRemoveButtonsList = listOf(addPlayerButton, removePlayerButton)
        setElementsInRows(addRemoveButtonsList)
        val addRemoveContainer = GuiWindow(
            drawer, scale = playerButtonSize, color = Color.TRANSPARENT,
            draggable = false, childElements = addRemoveButtonsList.toMutableList()
        )

        val playerButtons = players.withIndex()
            .map { (index, player) ->
                val playerName = if (player.name.length == 1) "" else player.name
                GuiInput(drawer, scale = Vec2(75f, 50f), placeholder = "Player ${index + 1}",
                    onClick = setTextInputState, onChange = { text -> player.name = text })
                    .setTextValue(playerName)
            }
            .let { listOf(addRemoveContainer) + it }
        setElementsInColumns(playerButtons, 40f)
        when (indexOfPlayersButtons) {
            -1 -> elements.addAll(playerButtons)
            else -> elements.addAll(indexOfPlayersButtons, playerButtons)
        }

        playerButtons.forEach { it.id = GuiElementIdentifierType.ADD_PLAYERS_GROUP }
    }

    fun createPlayersPickShields(player: GamePlayer, onClickShield: (player: GamePlayer) -> Unit) {
        clear()
        val shieldPickerWindow =
            GuiWindow(
                drawer, Vec2(200f, -200f), Vec2(150f, 150f), title = "${player.name} to pick a shield",
                draggable = true
            )
        shieldPickerWindow.addChild(
            GuiButton(drawer, scale = Vec2(100f, 25f), title = "Pick one", onClick = { onClickShield(player) })
        )
        elements.add(shieldPickerWindow)
    }

    fun createPlayerCommandPanel(
        player: GamePlayer,
        onClickAim: (player: GamePlayer) -> Unit,
        onClickPower: (player: GamePlayer) -> Unit,
        onClickFire: (player: GamePlayer) -> Unit
    ) {
        clear()
        val commandPanelWindow = GuiWindow(
            drawer, Vec2(350f, -350f), Vec2(150f, 150f),
            title = player.name, draggable = true
        )
        commandPanelWindow.addChildren(
            listOf(
                GuiButton(drawer, Vec2(-100f, 0f), Vec2(50f, 25f), title = "Aim",
                    onClick = { onClickAim(player) }),
                GuiButton(drawer, Vec2(-100f, -50f), Vec2(50f, 25f), title = "Power",
                    onClick = { onClickPower(player) }),
                GuiButton(drawer, Vec2(-100f, -100f), Vec2(50f, 25f), title = "Fire",
                    onClick = { onClickFire(player) }),

                GuiLabel(drawer, Vec2(0f, 90f), getPlayerAimAngleDisplay(player), .15f,
                    updateCallback = { it.title = getPlayerAimAngleDisplay(player) }),
                GuiLabel(drawer, Vec2(0f, 70f), getPlayerAimPowerDisplay(player), .15f,
                    updateCallback = { it.title = getPlayerAimPowerDisplay(player) })
            )
        )
        elements.add(commandPanelWindow)
    }

    private fun getPlayerAimPowerDisplay(player: GamePlayer): String =
        player.playerAim.power.let { displayNumber(it, 2) + "%" }

    private fun getPlayerAimAngleDisplay(player: GamePlayer): String =
        player.playerAim.getDegreesAngle().let { displayNumber(it, 2) + "º" }

    fun createRoundLeaderboard(players: MutableList<GamePlayer>, onClickNextRound: () -> Unit) {
        clear()
        val leaderBoardWindow = GuiWindow(drawer, Vec2(), Vec2(200f, 300f), "Leaderboard", draggable = true)
        val playerLines = players.sortedByDescending { it.score }.map {
            GuiLabel(
                drawer,
                title = "${it.name.padEnd(10, ' ')}${it.score.roundToInt()}".padStart(10, ' '),
                textSize = .2f
            )
        }
        setElementsInRows(playerLines, 10f)

        leaderBoardWindow.addChildren(playerLines)
        leaderBoardWindow.addChild(
            GuiButton(
                drawer, Vec2(0f, -280f), Vec2(100f, 25f), "Next Round",
                onClick = onClickNextRound
            )
        )
        elements.add(leaderBoardWindow)
    }

    private fun displayNumber(value: Float, decimals: Int): String = roundFloat(value, decimals).toString()

    private fun setElementsInColumns(elements: List<GuiElement>, gap: Float = 0f, centered: Boolean = true) {
        val totalWidth = elements.map { it.scale.x * 2f }.sum() + gap * (elements.size - 1)
        val columnSize = totalWidth / elements.size

        elements.withIndex()
            .forEach { (index, element) ->
                val newXOffset = when (centered) {
                    true -> columnSize * (index - (elements.size - 1) * .5f)
                    else -> columnSize * index + columnSize * .5f
                }
                element.addOffset(Vec2(newXOffset, element.offset.y))
            }
    }

    private fun setElementsInRows(elements: List<GuiElement>, gap: Float = 0f, centered: Boolean = true) {
        val totalHeight = elements.map { it.scale.y * 2f }.sum() + gap * (elements.size - 1)
        val rowSize = totalHeight / elements.size

        elements.withIndex()
            .forEach { (index, element) ->
                val newYOffset = when (centered) {
                    true -> rowSize * (index - (elements.size - 1) * .5f)
                    else -> rowSize * index + rowSize * .5f
                } * -1f
                element.addOffset(Vec2(element.offset.x, newYOffset))
            }
    }

}
