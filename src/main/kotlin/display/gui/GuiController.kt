package display.gui

import display.draw.Drawer
import display.draw.TextureEnum
import display.graphic.Color
import display.text.TextJustify
import game.GamePlayer
import input.CameraView
import org.jbox2d.common.Vec2
import utility.Common.roundFloat
import utility.Common.vectorUnit
import kotlin.math.ceil
import kotlin.math.roundToInt

class GuiController(private val drawer: Drawer) {

    private val elements = mutableListOf<GuiElement>()

    fun render() = elements.forEach { it.render(null) }

    fun update() = elements.forEach { it.update() }

    fun clear() = elements.clear()

    fun checkHover(location: Vec2) = elements.forEach { it.handleHover(location) }

    fun checkLeftClick(location: Vec2) = elements.toList().forEach { it.handleLeftClick(location) }

    fun checkLeftClickDrag(location: Vec2, movement: Vec2) =
        elements.forEach { it.handleLeftClickDrag(location, movement) }

    fun checkScroll(movement: Vec2, location: Vec2) = elements.forEach { it.handleScroll(location, movement) }

    fun checkAddTextInput(text: String) = elements.filterIsInstance<GuiInput>().forEach { it.handleAddTextInput(text) }

    fun checkRemoveTextInput() = elements.filterIsInstance<GuiInput>().forEach { it.handleRemoveTextInput() }

    fun stopTextInput() = elements.filterIsInstance<GuiInput>().forEach { it.stopTextInput() }

    fun textInputIsBusy(): Boolean = elements.filterIsInstance<GuiInput>().toList().any { it.textInputIsBusy }

    fun locationIsGui(location: Vec2): Boolean = elements.any { it.isHover(location) }

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

        elements.add(GuiLabel(drawer, Vec2(-10f, 250f), TextJustify.CENTER, "Volynov", .6f))
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
        elements.add(GuiLabel(drawer, Vec2(0f, 250f), TextJustify.CENTER, "Select player names", .2f))

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
            onClick = if (players.size > 2) onRemovePlayer else noOpCallback
        )
        val addRemoveButtonsList = listOf(addPlayerButton, removePlayerButton)
        setElementsInRows(addRemoveButtonsList)
        val addRemoveContainer = GuiPanel(
            drawer, scale = playerButtonSize, color = Color.TRANSPARENT,
            draggable = false, childElements = addRemoveButtonsList.toMutableList()
        )

        val playerButtons = players.withIndex()
            .map { (index, player) ->
                val playerName = if (player.name.length == 1) "" else player.name
                GuiInput(drawer, scale = Vec2(75f, 50f), placeholder = "Player ${index + 1}",
                    onChange = { text -> player.name = text })
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

    fun addPlayerLabels(players: List<GamePlayer>, camera: CameraView) {
        val color = Color.WHITE.setAlpha(.7f)
        val textSize = .1f
        val justify = TextJustify.CENTER
        elements.addAll(
            players.flatMap { player ->
                val vehicle = player.vehicle!!

                val updateNameCallback = { element: GuiElement ->
                    val screenLocation = camera.getGuiLocation(vehicle.worldBody.position.add(Vec2(0f, vehicle.radius)))
                        .add(Vec2(0f, 22f))
                    element.updateOffset(screenLocation)
                }
                val name = GuiLabel(drawer, Vec2(), justify, player.name, textSize, color, updateNameCallback)

                val updateHpCallback = { element: GuiElement ->
                    val screenLocation = name.offset.add(Vec2(0f, -15f))
                    element.updateOffset(screenLocation)
                    element.title = "HP ${ceil(vehicle.hitPoints).toInt()}"
                }
                val hitPoints = GuiLabel(drawer, Vec2(), justify, "", textSize, color, updateHpCallback)

                listOf(name, hitPoints)
            }.map {
                it.updateCallback(it)
                it.scale.setZero()
                it
            }
        )
    }

    fun createPlayersPickShields(player: GamePlayer, onClickShield: (player: GamePlayer) -> Unit) {
        clear()
        val shieldPickerPanel =
            GuiPanel(
                drawer, Vec2(710f, -340f), Vec2(250f, 200f), title = "${player.name} to pick a shield",
                draggable = true
            )
        shieldPickerPanel.addChild(
            GuiButton(drawer, scale = Vec2(100f, 25f), title = "Pick one", onClick = { onClickShield(player) })
        )
        elements.add(shieldPickerPanel)
    }

    fun createPlayerCommandPanel(
        player: GamePlayer,
        onClickAim: (player: GamePlayer) -> Unit,
        onClickPower: (player: GamePlayer) -> Unit,
        onClickFire: (player: GamePlayer) -> Unit
    ) {
        clear()
        val commandPanel = GuiPanel(
            drawer, Vec2(710f, -340f), Vec2(250f, 200f),
            title = player.name, draggable = true
        )
        val weaponsList = GuiScroll(drawer, Vec2(50f, -50f), Vec2(100f, 100f)).addChildren(
            (1..5).map {
                GuiButton(drawer, scale = Vec2(100f, 25f), title = "Boom $it", textSize = .15f,
                    onClick = { println("clicked [Boom $it]") })
            }
        )
        val actionButtonScale = Vec2(50f, 25f)
        val actionButtonsOffset = Vec2(-100f, -50f)
        val statsOffset = Vec2(40f, 190f)
        commandPanel.addChildren(
            listOf(
                GuiButton(drawer, actionButtonsOffset.clone(), actionButtonScale, title = "Aim",
                    onClick = { onClickAim(player) }),
                GuiButton(drawer, actionButtonsOffset.clone(), actionButtonScale, title = "Power",
                    onClick = { onClickPower(player) }),
                GuiButton(drawer, actionButtonsOffset.clone(), actionButtonScale, title = "Fire",
                    onClick = { onClickFire(player) })
            ).also { setElementsInRows(it, centered = false) }
                    + listOf(
                GuiLabel(drawer, Vec2(-210f, 110f), TextJustify.LEFT, getPlayerAimAngleDisplay(player), .15f,
                    updateCallback = { it.title = getPlayerAimAngleDisplay(player) }),
                GuiLabel(drawer, Vec2(-210f, 80f), TextJustify.LEFT, getPlayerAimPowerDisplay(player), .15f,
                    updateCallback = { it.title = getPlayerAimPowerDisplay(player) }),

                weaponsList,

                GuiIcon(drawer, Vec2(-230f, 110f), vectorUnit.mul(20f), texture = TextureEnum.icon_aim)
            )
                    + listOf(
                GuiLabel(drawer, statsOffset.clone(), TextJustify.LEFT,
                    "HP      ${ceil(player.vehicle!!.hitPoints).toInt()}%", .15f),
                GuiLabel(drawer, statsOffset.clone(), TextJustify.LEFT,
                    "Energy ${ceil(player.vehicle!!.shield!!.energy).toInt()}%", .15f),
                GuiLabel(drawer, statsOffset.clone(), TextJustify.LEFT,
                    "Wealth ${player.cash.toInt()}", .15f)
            ).also { setElementsInRows(it, centered = false) }
        )
        elements.add(commandPanel)
    }

    private fun getPlayerAimPowerDisplay(player: GamePlayer): String =
        player.playerAim.power.let { displayNumber(it, 2) + "%" }

    private fun getPlayerAimAngleDisplay(player: GamePlayer): String =
        player.playerAim.getDegreesAngle().let { displayNumber(it, 2) + "º" }

    fun createRoundLeaderboard(players: MutableList<GamePlayer>, onClickNextRound: () -> Unit) {
        clear()
        val leaderBoardPanel = GuiPanel(drawer, Vec2(), Vec2(200f, 300f), "Leaderboard", draggable = false)
        val playerLines = listOf(GuiLabel(
            drawer,
            Vec2(-50f, 100f),
            justify = TextJustify.LEFT,
            title = "Player          Score".padStart(10, ' '),
            textSize = .2f
        )) + players.sortedByDescending { it.score }.map {
            GuiLabel(
                drawer,
                Vec2(-50f, 100f),
                justify = TextJustify.LEFT,
                title = "${it.name.padEnd(20, ' ')}${it.score.roundToInt()}".padStart(10, ' '),
                textSize = .2f
            )
        }
        setElementsInRows(playerLines, 10f)

        leaderBoardPanel.addChildren(playerLines)
        leaderBoardPanel.addChild(
            GuiButton(
                drawer, Vec2(0f, -250f), Vec2(100f, 25f), "Next Match",
                onClick = onClickNextRound
            )
        )
        elements.add(leaderBoardPanel)
    }

    private fun displayNumber(value: Float, decimals: Int): String = roundFloat(value, decimals).toString()

    companion object {

        fun setElementsInColumns(elements: List<GuiElement>, gap: Float = 0f, centered: Boolean = true) {
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

        fun setElementsInRows(elements: List<GuiElement>, gap: Float = 0f, centered: Boolean = true) {
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

}
