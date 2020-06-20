package display.gui

import display.Window
import display.draw.Drawer
import display.draw.TextureEnum
import display.graphic.Color
import display.gui.LayoutController.getOffsetForLayoutPosition
import display.gui.LayoutController.setElementsInColumns
import display.gui.LayoutController.setElementsInRows
import display.text.TextJustify
import game.GamePlayer
import input.CameraView
import org.jbox2d.common.Vec2
import utility.Common.makeVec2
import utility.Common.roundFloat
import kotlin.math.ceil
import kotlin.math.roundToInt

class GuiController(private val drawer: Drawer, val window: Window) {

    private val windowSize
        get() = makeVec2(window.width, window.height)

    private val elements = mutableListOf<GuiElement>()

    fun render() = elements.forEach { it.render(null) }

    fun update() = elements.forEach { it.update() }

    fun clear() = elements.clear()

    fun checkHover(location: Vec2) = elements.filterIsInstance<HasHover>()
        .forEach { it.handleHover(location) }

    fun checkLeftClickPress(location: Vec2) = elements.toList().filterIsInstance<HasClick>()
        .any { it.handleLeftClickPress(location) }

    fun checkLeftClickRelease(location: Vec2) = elements.toList().filterIsInstance<HasClick>()
        .any { it.handleLeftClickRelease(location) }

    fun checkLeftClickDrag(location: Vec2, movement: Vec2): Boolean {
        return elements.filterIsInstance<HasDrag>()
            .any { it.handleLeftClickDrag(location, movement) }
    }

    fun checkScroll(movement: Vec2, location: Vec2): Boolean {
        return elements.filterIsInstance<HasScroll>()
            .any { it.handleScroll(location, movement) } or
                elements.filterIsInstance<HasKids>()
                    .any { it.handleScroll(location, movement) }
    }

    fun checkAddTextInput(text: String) = elements.filterIsInstance<GuiInput>().any { it.handleAddTextInput(text) }

    fun checkRemoveTextInput() = elements.filterIsInstance<GuiInput>().any { it.handleRemoveTextInput() }

    fun stopTextInput() = elements.filterIsInstance<GuiInput>().any { it.stopTextInput() }

    fun textInputIsBusy(): Boolean = elements.filterIsInstance<GuiInput>().toList().any { it.textInputIsBusy }

    fun locationIsGui(location: Vec2): Boolean = elements.filterIsInstance<HasHover>()
        .any { it.isHover(location) }

    fun createMainMenu(
        onClickNewGame: () -> Unit,
        onClickSettings: () -> Unit,
        onClickQuit: () -> Unit
    ) {
        clear()
        val buttonScale = Vec2(200f, 44f)

        val title = GuiLabel(drawer, Vec2(0f, 300f), TextJustify.CENTER, "Volynov", .6f)
        val newGameButton = GuiButton(drawer, scale = buttonScale, title = "New Game",
            textSize = .27f, onClick = onClickNewGame)
        setElementsInRows(listOf(title, newGameButton), 80f, false)

        val menuButtons = listOf(
            newGameButton,
            GuiButton(drawer, scale = buttonScale, title = "Settings", textSize = .27f, onClick = onClickSettings),
            GuiButton(drawer, scale = buttonScale, title = "Credits", textSize = .27f, onClick = {}),
            GuiButton(drawer, scale = buttonScale, title = "Quit", textSize = .27f, onClick = onClickQuit))
            .also { setElementsInRows(it, 40f, false) }

        elements.add(title)
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
        elements.add(GuiLabel(drawer, Vec2(0f, 250f), TextJustify.CENTER, "Select Players", .2f))
        elements.add(GuiLabel(drawer, Vec2(-200f, 200f), TextJustify.LEFT,
            "Press the [+] button to add more players", .12f))
        elements.add(GuiLabel(drawer, Vec2(-200f, 180f), TextJustify.LEFT, "Use the input box to type in names", .12f))

        updateMainMenuSelectPlayers(playerList, onAddPlayer, onRemovePlayer)

        val actionButtonSize = Vec2(100f, 25f)
        val actionButtons = listOf(
            GuiButton(drawer, scale = actionButtonSize, title = "Cancel",
                textSize = .15f, onClick = onClickCancel),
            GuiButton(drawer, scale = actionButtonSize, title = "Start", textSize = .15f, onClick = onClickStart))
            .also { buttons ->
                setElementsInColumns(buttons, 40f)
                buttons.forEach { it.addOffset(Vec2(0f, -150f)) }
            }

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
        val playerButtonHalfSize = playerButtonSize.clone().also { it.y *= .5f }

        val addPlayerButton = GuiButton(drawer, scale = playerButtonHalfSize, title = " + ", textSize = .3f,
            onClick = if (players.size < 6) onAddPlayer else noOpCallback)
        val removePlayerButton = GuiButton(drawer, scale = playerButtonHalfSize, title = " - ", textSize = .3f,
            onClick = if (players.size > 2) onRemovePlayer else noOpCallback)
        val addRemoveButtons = listOf(addPlayerButton, removePlayerButton)
            .also { setElementsInRows(it) }

        val addRemoveContainer = GuiPanel(drawer, scale = playerButtonSize, color = Color.TRANSPARENT,
            draggable = false).also { it.addKids(addRemoveButtons) }

        val playerButtons = (listOf(addRemoveContainer) +
                players.withIndex()
                    .map { (index, player) ->
                        val playerName = if (player.name.length == 1) "" else player.name
                        GuiInput(drawer, scale = Vec2(75f, 50f), placeholder = "Player ${index + 1}",
                            onChange = { text -> player.name = text })
                            .setTextValue(playerName)
                    })
            .also { setElementsInColumns(it, 40f) }

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
                    (element as HasLabel).title = "HP ${ceil(vehicle.hitPoints).toInt()}"
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
        val shieldPickerPanel = GuiPanel(drawer, scale = Vec2(250f, 200f),
            title = "${player.name} to pick a shield", draggable = true)
            .also {
                it.updateOffset(getOffsetForLayoutPosition(
                    LayoutPosition.BOTTOM_RIGHT, windowSize.mul(.5f), it.scale))
            }
        val shieldsList = GuiScroll(drawer, Vec2(50f, -50f), Vec2(100f, 100f)).addKids(
            (1..5).map {
                GuiButton(drawer, scale = Vec2(100f, 25f), title = "Shield $it", textSize = .15f,
                    onClick = {
                        onClickShield(player)
                        println("clicked [Shield $it]")
                    })
            }
        )
        shieldPickerPanel.addKid(GuiLabel(drawer, Vec2(-200f, 100f), TextJustify.LEFT,
            "Shields not yet implemented", .12f))
        shieldPickerPanel.addKid(shieldsList)

        elements.add(shieldPickerPanel)

        listOf(
            GuiLabel(drawer, title = "Right-click drag to move the camera", textSize = .12f),
            GuiLabel(drawer, title = "Mouse scroll to zoom the camera", textSize = .12f),
            GuiLabel(drawer, title = "Double-click on a planet to camera-track it", textSize = .12f),
            GuiLabel(drawer, title = "Left-click drag on a panel to move it", textSize = .12f),
            GuiLabel(drawer, title = "Destroy all opponents to win the round", textSize = .12f)
        ).also { labels ->
            setElementsInRows(labels, centered = false)
            labels.forEach { it.addOffset(Vec2(70f, -360f)) }
            elements.addAll(labels)
        }
    }

    fun createPlayerCommandPanel(
        player: GamePlayer,
        onClickAim: (player: GamePlayer) -> Unit,
        onClickPower: (player: GamePlayer) -> Unit,
        onClickFire: (player: GamePlayer) -> Unit
    ) {
        clear()
        val commandPanel = GuiPanel(drawer, scale = Vec2(250f, 200f), title = player.name, draggable = true)
            .also {
                it.updateOffset(getOffsetForLayoutPosition(
                    LayoutPosition.BOTTOM_RIGHT, windowSize.mul(.5f), it.scale))
            }

        val weaponButtonWidth = 150f
        val weaponsList = GuiScroll(drawer, scale = Vec2(weaponButtonWidth, 100f)).addKids(
            (1..15).map {
                GuiButton(drawer, scale = Vec2(weaponButtonWidth, 25f), title = "Boom $it", textSize = .15f,
                    onClick = { println("clicked [Boom $it]") })
            })
            .also {
                it.updateOffset(getOffsetForLayoutPosition(LayoutPosition.BOTTOM_RIGHT, commandPanel.scale, it.scale))
            }

        val actionButtonScale = Vec2(50f, 25f)
        val actionButtonsOffset = Vec2(-200f, -75f)
        val actionButtons = listOf(
            GuiButton(drawer, actionButtonsOffset.clone(), actionButtonScale, title = "Aim",
                onClick = { onClickAim(player) }),
            GuiButton(drawer, actionButtonsOffset.clone(), actionButtonScale, title = "Power",
                onClick = { onClickPower(player) }),
            GuiButton(drawer, actionButtonsOffset.clone(), actionButtonScale, title = "Fire",
                onClick = { onClickFire(player) }))
            .also { setElementsInRows(it, centered = false) }

        val iconAim = GuiIcon(drawer, scale = makeVec2(7), texture = TextureEnum.icon_aim_direction)
            .also {
                it.updateOffset(
                    getOffsetForLayoutPosition(LayoutPosition.TOP_LEFT, commandPanel.scale, it.scale))
            }
        val aimingInfo = listOf(
            iconAim,
            GuiIcon(drawer, scale = makeVec2(7), texture = TextureEnum.icon_aim_power))
            .also { icons ->
                setElementsInRows(icons, centered = false)
                icons.forEach { it.offset.x = iconAim.offset.x }
            }
            .zip(listOf(
                GuiLabel(drawer, Vec2(), TextJustify.LEFT, getPlayerAimAngleDisplay(player), .15f,
                    updateCallback = { (it as HasLabel).title = getPlayerAimAngleDisplay(player) }).also {
                    it.scale.set(makeVec2(20f))
                },
                GuiLabel(drawer, Vec2(), TextJustify.LEFT, getPlayerAimPowerDisplay(player), .15f,
                    updateCallback = { (it as HasLabel).title = getPlayerAimPowerDisplay(player) }).also {
                    it.scale.set(makeVec2(20f))
                }
            ))
            .also {
                it.forEach { (icon, label) ->
                    label.updateOffset(icon.offset)
                    setElementsInColumns(listOf(icon, label), centered = false)
                }
            }
            .flatMap { it.toList() }

        val playerStats = listOf(
            GuiLabel(drawer, justify = TextJustify.LEFT,
                title = "HP      ${ceil(player.vehicle!!.hitPoints).toInt()}%", textSize = .15f)
                .also {
                    it.scale.addLocal(50f, 0f)
                    it.updateOffset(
                        getOffsetForLayoutPosition(LayoutPosition.TOP_RIGHT, commandPanel.scale, it.scale))
                },
            GuiLabel(drawer, justify = TextJustify.LEFT,
                title = "Energy ${ceil(player.vehicle!!.shield!!.energy).toInt()}%", textSize = .15f),
            GuiLabel(drawer, justify = TextJustify.LEFT,
                title = "Wealth ${player.cash.toInt()}", textSize = .15f))
            .also { labels ->
                setElementsInRows(labels, centered = false)
                val align = labels.first().offset.x
                labels.forEach { it.offset.x = align }
            }

        commandPanel.addKids(
            actionButtons + aimingInfo + playerStats +
                    GuiLabel(drawer, Vec2(-30f, 30f), TextJustify.LEFT, "Weapons not yet implemented", .12f) +
                    weaponsList)
        elements.add(commandPanel)

        elements.add(GuiLabel(drawer, Vec2(-130f, -500f),
            title = "When setting aim/power, hover the mouse cursor near your vehicle", textSize = .12f))
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

        leaderBoardPanel.addKids(playerLines)
        leaderBoardPanel.addKid(
            GuiButton(
                drawer, Vec2(0f, -250f), Vec2(100f, 25f), "Next Match",
                onClick = onClickNextRound
            )
        )
        elements.add(leaderBoardPanel)
    }

    private fun displayNumber(value: Float, decimals: Int): String = roundFloat(value, decimals).toString()

}
