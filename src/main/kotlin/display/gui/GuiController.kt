package display.gui

import dI
import display.event.MouseButtonEvent
import display.graphic.Color
import display.gui.LayoutController.getOffsetForLayoutPosition
import display.gui.LayoutController.setElementsInColumns
import display.gui.LayoutController.setElementsInRows
import display.gui.LayoutPosition.BOTTOM_RIGHT
import display.gui.LayoutPosition.CENTER_RIGHT
import display.gui.base.*
import display.gui.element.*
import display.gui.special.GuiCommandPanel
import display.gui.special.MerchandiseLists
import display.text.TextJustify
import engine.freeBody.Vehicle
import game.GamePlayer
import game.shield.ShieldType
import game.shield.VehicleShield
import input.CameraView
import io.reactivex.Observable
import org.jbox2d.common.Vec2
import utility.Common.PiH
import utility.Common.makeVec2
import kotlin.math.ceil
import kotlin.math.roundToInt

class GuiController {

    private val window = dI.window

    private val windowSize
        get() = makeVec2(window.width, window.height)

    private val elements = mutableListOf<GuiElement>()

    val tooltip = GuiTooltip()

    fun render() {
        elements.forEach { it.render(null) }
        tooltip.render(null)
    }

    fun update() {
        elements.forEach { it.update() }
        tooltip.update()
    }

    fun clear() {
        elements.clear()
        tooltip.clear()
    }

    fun sendLeftClick(startEvent: MouseButtonEvent, event: Observable<MouseButtonEvent>) = elements.toList()
        .filterIsInstance<HasClick>().any { it.handleLeftClick(startEvent, event) }

    fun checkHover(location: Vec2) = elements.filterIsInstance<HasHover>()
        .forEach { it.handleHover(location) }

    fun checkScroll(movement: Vec2, location: Vec2): Boolean {
        return elements.filterIsInstance<HasScroll>()
            .any { it.handleScroll(location, movement) } or
                elements.filterIsInstance<HasKids>()
                    .any { it.handleScroll(location, movement) }
    }

    fun locationIsGui(location: Vec2): Boolean = elements.filterIsInstance<HasHover>()
        .any { it.isHover(location) }

    fun dispose() {
        tooltip.dispose()
    }

    fun createMainMenu(
        onClickNewGame: () -> Unit,
        onClickSettings: () -> Unit,
        onClickQuit: () -> Unit
    ) {
        clear()
        val buttonScale = Vec2(200f, 44f)

        val title = GuiLabel(Vec2(0f, 300f), TextJustify.CENTER, "Volynov", .6f)
        val newGameButton =
            GuiButton(scale = buttonScale, title = "New Game", textSize = .27f,
                onClick = onClickNewGame)
        setElementsInRows(listOf(title, newGameButton), 80f, false)

        val menuButtons = listOf(
            newGameButton,
            GuiButton(scale = buttonScale, title = "Settings", textSize = .27f,
                onClick = onClickSettings),
            GuiButton(scale = buttonScale, title = "Credits", textSize = .27f,
                onClick = {}),
            GuiButton(scale = buttonScale, title = "Quit", textSize = .27f,
                onClick = onClickQuit))
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
        elements.add(
            GuiLabel(Vec2(0f, 250f), TextJustify.CENTER, "Select Players", .2f))
        elements.add(GuiLabel(Vec2(-200f, 200f), TextJustify.LEFT,
            "Press the [+] button to add more players",
            .12f))
        elements.add(GuiLabel(Vec2(-200f, 180f), TextJustify.LEFT,
            "Use the input box to type in names", .12f))

        updateMainMenuSelectPlayers(playerList, onAddPlayer, onRemovePlayer)

        val actionButtonSize = Vec2(100f, 25f)
        val actionButtons = listOf(
            GuiButton(scale = actionButtonSize, title = "Cancel", textSize = .15f, onClick = onClickCancel),
            GuiButton(scale = actionButtonSize, title = "Start", textSize = .15f, onClick = onClickStart))
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

        val addPlayerButton =
            GuiButton(scale = playerButtonHalfSize, title = " + ", textSize = .3f,
                onClick = if (players.size < 6) onAddPlayer else noOpCallback)
        val removePlayerButton =
            GuiButton(scale = playerButtonHalfSize, title = " - ", textSize = .3f,
                onClick = if (players.size > 2) onRemovePlayer else noOpCallback)
        val addRemoveButtons = listOf(addPlayerButton, removePlayerButton)
            .also { setElementsInRows(it) }

        val addRemoveContainer = GuiPanel(scale = playerButtonSize,
            color = Color.TRANSPARENT, draggable = false).also {
            it.addKids(addRemoveButtons)
        }

        val playerButtons = (listOf(addRemoveContainer) +
                players.withIndex()
                    .map { (index, player) ->
                        val playerName = if (player.name.length == 1) "" else player.name
                        GuiInput(scale = Vec2(75f, 50f),
                            placeholder = "Player ${index + 1}",
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

    fun addPlayerFakeLabels(vehiclesAndHps: List<Pair<Vehicle, Float>>, camera: CameraView)
            : HashMap<Vehicle, GuiLabel> {
        val color = Color.WHITE.setAlpha(.7f)
        val textSize = .1f
        val justify = TextJustify.CENTER

        return vehiclesAndHps.map { (vehicle, hp) ->
            val updateNameOffsetCallback = { element: GuiElement ->
                val offsetAboveVehicle = vehicle.worldBody.position.add(Vec2(0f, vehicle.radius))
                val screenLocation = camera.getGuiLocation(offsetAboveVehicle)
                    .add(Vec2(0f, 22f))
                element.updateOffset(screenLocation)
            }
            val nameLabel = GuiLabel(Vec2(), justify, vehicle.player.name, textSize, color, updateNameOffsetCallback)

            val updateHpOffsetCallback = { element: GuiElement ->
                val screenLocation = nameLabel.offset.add(Vec2(0f, -15f))
                element.updateOffset(screenLocation)
            }
            val hitPointsLabel = GuiLabel(Vec2(), justify, "HP ${ceil(hp).toInt().coerceAtLeast(0)}",
                textSize, color, updateHpOffsetCallback)

            elements.add(nameLabel)
            elements.add(hitPointsLabel)

            nameLabel.updateCallback(nameLabel)
            hitPointsLabel.updateCallback(hitPointsLabel)

            nameLabel.scale.setZero()
            hitPointsLabel.scale.setZero()

            Pair(vehicle, hitPointsLabel)
        }.let { HashMap(it.toMap()) }
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
                val name = GuiLabel(Vec2(), justify, player.name, textSize, color, updateNameCallback)

                val updateHpCallback = { element: GuiElement ->
                    val screenLocation = name.offset.add(Vec2(0f, -15f))
                    element.updateOffset(screenLocation)
                    (element as HasLabel).title = "HP ${ceil(vehicle.hitPoints).toInt().coerceAtLeast(0)}"
                }
                val hitPoints = GuiLabel(Vec2(), justify, "", textSize, color,
                    updateHpCallback)

                listOf(name, hitPoints)
            }.map {
                it.updateCallback(it)
                it.scale.setZero()
                it
            }
        )
    }

    fun createPlayersPickShields(player: GamePlayer, onClickShield: () -> Unit) {
        clear()
        val tabsContainerSize = Vec2(195f, 165f)
        val scrollButtonScale = Vec2(tabsContainerSize.x, 22f)

        val shieldPickerPanel = GuiPanel(scale = tabsContainerSize.clone().add(Vec2(5f, 20f)),
            title = "${player.name} select a shield",
            draggable = false)
            .also {
                it.updateOffset(getOffsetForLayoutPosition(
                    BOTTOM_RIGHT, windowSize.mul(.5f), it.scale))
            }

        val allMerchandise = MerchandiseLists()
        val shieldsList = GuiScroll(scale = tabsContainerSize.clone())
            .also { scrollBox ->
                scrollBox.addKids(VehicleShield.descriptor.entries
                    .sortedBy { it.value.order }
                    .map { (key, value) ->
                        GuiMerchandise(scale = scrollButtonScale.clone(), name = value.name, price = value.price,
                            itemId = value.order.toString(), description = value.description, key = key.toString(),
                            onClick = {
                                player.playerAim.setSelectedShield(key, allMerchandise, player)
                                onClickShield()
                            })
                    })
                allMerchandise.shields = scrollBox.kidElements.filterIsInstance<GuiMerchandise>()
                scrollBox.placeOnEdge(BOTTOM_RIGHT, shieldPickerPanel.scale)
            }
        player.playerAim.setSelectedShield(null, allMerchandise, player)

        shieldPickerPanel.addKid(shieldsList)
        elements.add(shieldPickerPanel)

        listOf(
            GuiLabel(title = "Right-click drag to move the camera", textSize = .11f),
            GuiLabel(title = "Mouse scroll to zoom the camera", textSize = .11f),
            GuiLabel(title = "Double-click on a planet to camera-track it", textSize = .11f),
            GuiLabel(title = "Left-click drag on a panel to move it", textSize = .11f),
            GuiLabel(title = "Destroy all opponents to win the round", textSize = .11f)
        ).also { labels ->
            setElementsInRows(labels, centered = false)
            labels.forEach { it.addOffset(Vec2(-350f, -450f)) }
            elements.addAll(labels)
        }
    }

    fun createPlayerCommandPanel(
        player: GamePlayer,
        onClickAim: () -> Unit,
        onChangeAim: () -> Unit,
        onClickMove: (player: GamePlayer) -> Unit,
        onClickFire: (player: GamePlayer) -> Unit
    ) {
        clear()
        val commandPanel = GuiCommandPanel(player, onClickAim, onChangeAim, onClickMove, onClickFire)
        elements.add(commandPanel)
        elements.add(GuiLabel(Vec2(-450f, -450f),
            title = "When setting aim/power, hover the mouse cursor near your vehicle",
            textSize = .11f, maxWidth = 150f))
    }

    fun createJumpFuelBar(player: GamePlayer) {
        clear()
        elements.add(GuiProgressBar(Vec2(), Vec2(150f, 10f), PiH,
            onDrag = { value: Float -> player.playerAim.precision = value })
        { e -> (e as GuiProgressBar).progressTarget = player.vehicle!!.fuel!!.amount * .01f }
            .also { it.placeOnEdge(CENTER_RIGHT, windowSize.mul(.5f)) }
        )
    }

    fun createRoundLeaderboard(players: MutableList<GamePlayer>, onClickNextRound: () -> Unit) {
        clear()
        val leaderBoardPanel =
            GuiPanel(Vec2(), Vec2(200f, 300f), "Leaderboard", draggable = false)
        val playerLines = listOf(GuiLabel(
            Vec2(-50f, 100f),
            justify = TextJustify.LEFT,
            title = "Player          Score".padStart(10, ' '),
            textSize = .2f
        )) + players.sortedByDescending { it.score }.map {
            GuiLabel(
                Vec2(-50f, 100f),
                justify = TextJustify.LEFT,
                title = "${it.name.padEnd(20, ' ')}${it.score.roundToInt()}".padStart(10, ' '),
                textSize = .2f
            )
        }
        setElementsInRows(playerLines, 10f)

        leaderBoardPanel.addKids(playerLines)
        leaderBoardPanel.addKid(GuiButton(Vec2(0f, -250f), Vec2(100f, 25f), "Next Match", onClick = onClickNextRound))
        elements.add(leaderBoardPanel)
    }

    fun cycleActiveElement(activeElement: GuiElement, reverse: Boolean = false) {
        val flatListElements = elements.filter { it !is HasKids }
            .union(elements.filterIsInstance<HasKids>().flatMap { it.getFlatListKidElements() })
            .toList()
            .let { if (reverse) it.asReversed() else it }

        val activeElementIndex = flatListElements.indexOf(activeElement)
        val nextElement = flatListElements.subList(activeElementIndex + 1, flatListElements.size)
            .find { it is GuiInput }
            ?: flatListElements.find { it is GuiInput }

        (nextElement as GuiInput).setActive()
    }

}
