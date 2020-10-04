package display.gui.special

import dI
import display.draw.TextureEnum
import display.graphic.Color
import display.gui.LayoutController
import display.gui.LayoutPosition
import display.gui.base.GuiElement
import display.gui.base.HasLabel
import display.gui.element.*
import game.GamePlayer
import game.fuel.Fuel
import game.shield.ShieldType
import game.shield.VehicleShield
import org.jbox2d.common.Vec2
import utility.Common
import utility.Common.muCron
import utility.Common.makeVec2
import kotlin.math.ceil

class GuiCommandPanel(player: GamePlayer,
                      onClickAim: () -> Unit,
                      onChangeAim: () -> Unit,
                      onClickMove: (player: GamePlayer) -> Unit,
                      onClickFire: (player: GamePlayer) -> Unit)
    : GuiPanel(
    scale = Vec2(300f, 200f),
    title = player.name,
    draggable = true
) {

    private val window = dI.window
    private val windowSize
        get() = makeVec2(window.width, window.height)

    init {
        placeOnEdge(LayoutPosition.BOTTOM_RIGHT, windowSize.mul(.5f))

        val tabs = getBuyItemsTabsContainer(player, this)
        val actionButtons = getActionButtons(player, onClickMove, onClickFire)
        val aimingInfo = getAimSetter(player, onChangeAim, onClickAim)
        val playerStats = getPlayerStats(player)
        val shoppingCart = getShoppingCart(player)

        addKids(actionButtons + aimingInfo + playerStats + tabs + shoppingCart +
                GuiLabel(Vec2(-90f, 130f),
                    title = "Weapons not implemented", textSize = .11f))
    }

    private fun getShoppingCart(player: GamePlayer): List<GuiLabel> {
        return listOf(
            GuiLabel(title = "Selected Equipment", textSize = .1f),
            GuiLabel(title = "Weapon", textSize = .1f),
            GuiLabel(textSize = .1f, updateCallback = { e ->
                (e as GuiLabel).title = "Shield    ${
                player.playerAim.selectedShieldDescriptor?.name ?: ""}"
            }),
            GuiLabel(textSize = .1f, updateCallback = { e ->
                (e as GuiLabel).title = "Fuel      ${
                player.playerAim.selectedFuelDescriptor?.name ?: ""}"
            }))
            .also { labels ->
                val first = labels.first()
                labels.forEach { it.updateScale(Vec2(1f, 10f)) }
                val totalScale = Vec2(first.scale.x, labels.map { it.scale.y * 1.66f }.sum())
                first.placeOnEdge(LayoutPosition.BOTTOM_LEFT, scale, totalScale.add(makeVec2(10f)))
                labels.drop(1).forEach { it.addOffset(Vec2(first.offset.x, 0f)) }
                LayoutController.setElementsInRows(labels, centered = false)
            }
    }

    private fun getPlayerStats(player: GamePlayer): List<GuiLabel> {
        val hp = ceil(player.vehicle!!.hitPoints).toInt()
        val energy = ceil(player.vehicle!!.shield?.energy ?: 0f).toInt()
        val cash = player.cash.toInt()

        return listOf(
            GuiLabel(title = "HP      $hp%", textSize = .12f),
            GuiLabel(title = "Energy $energy", textSize = .12f),
            GuiLabel(title = "Cash    $cash$muCron", textSize = .12f))
            .also { labels ->
                labels.forEach { it.updateScale(Vec2(1f, 10f)) }
                val first = labels.first()
                first.placeOnEdge(LayoutPosition.TOP_LEFT, scale, first.scale.add(makeVec2(10f)))
                labels.drop(1).forEach { it.addOffset(Vec2(first.offset.x, 0f)) }
                LayoutController.setElementsInRows(labels, centered = false)
            }
    }

    private fun getAimSetter(player: GamePlayer, onChangeAim: () -> Unit, onClickAim: () -> Unit): List<GuiElement> {
        val iconScale = makeVec2(14)
        val iconPadding = makeVec2(3)
        val iconAim = GuiIcon(scale = iconScale, texture = TextureEnum.icon_aim_direction, padding = iconPadding)
        val iconPower = GuiIcon(scale = iconScale, texture = TextureEnum.icon_aim_power, padding = iconPadding)
        val buttonTarget = GuiButton(scale = iconScale, icon = TextureEnum.icon_target, onClick = onClickAim)

        return listOf(
            GuiSpinner(
                onClickMore = {
                    player.playerAim.addAngle()
                    onChangeAim()
                },
                onClickLess = {
                    player.playerAim.addAngle(-1f)
                    onChangeAim()
                },
                labelCallback = { (it as HasLabel).title = getPlayerAimAngleDisplay(player) }),
            GuiSpinner(
                onClickMore = {
                    player.playerAim.addPower()
                    onChangeAim()
                },
                onClickLess = {
                    player.playerAim.addPower(-1f)
                    onChangeAim()
                },
                labelCallback = { (it as HasLabel).title = getPlayerAimPowerDisplay(player) }),
            GuiProgressBar(scale = Vec2(61f, 10f), title = "Precision", color = Color.WHITE.setAlpha(.3f),
                onDrag = { value: Float -> player.playerAim.setPrecisionFromBar(value) })
            { e -> (e as GuiProgressBar).progressTarget = player.playerAim.getPrecisionForBar() })
            .also { elements ->
                val first = elements.first()
                first.placeOnEdge(LayoutPosition.CENTER_LEFT, scale, makeVec2(74f))
                first.addOffset(Vec2(0f, 90f))
                elements.drop(1).forEach { it.addOffset(Vec2(first.offset.x, 0f)) }
                LayoutController.setElementsInRows(elements, 10f, false)
            }
            .zip(listOf(iconAim, iconPower, buttonTarget))
            .flatMap { (left, right) ->
                right.updateOffset(left.offset.add(Vec2(80f, 0f)))
                listOf(left, right)
            }
    }

    private fun getPlayerAimAngleDisplay(player: GamePlayer): String =
        player.playerAim.getDegreesAngle().let { displayNumber(it, 2) + "º" }

    private fun getPlayerAimPowerDisplay(player: GamePlayer): String =
        player.playerAim.power.let { displayNumber(it, 2) + "%" }

    private fun displayNumber(value: Float, decimals: Int): String =
        Common.roundFloat(value, decimals).toString()

    private fun getActionButtons(player: GamePlayer,
                                 onClickMove: (player: GamePlayer) -> Unit,
                                 onClickFire: (player: GamePlayer) -> Unit): List<GuiButton> {
        val actionButtonScale = Vec2(50f, 25f) // dragHandleScale = Vec2(90f, 25f)
        return listOf(
            GuiButton(scale = actionButtonScale, title = "Fire", textSize = .12f,
                onClick = { onClickFire(player) }),
            GuiButton(scale = actionButtonScale, title = "Jump", textSize = .12f,
                onClick = { onClickMove(player) }))
            .also { buttons ->
                val first = buttons.first()
                val totalScale = Vec2(buttons.map { it.scale.x * 1.5f }.sum(), first.scale.y)
                first.placeOnEdge(LayoutPosition.TOP_RIGHT, scale, totalScale)
                buttons.drop(1).forEach { it.addOffset(Vec2(0f, first.offset.y)) }
                LayoutController.setElementsInColumns(buttons, centered = false)
            }
    }

    private fun getBuyItemsTabsContainer(player: GamePlayer, commandPanel: GuiPanel): GuiTabs {
        val tabsContainerSize = Vec2(195f, 165f)
        val scrollButtonScale = Vec2(tabsContainerSize.x, 22f)
        val scrollButtonTextSize = .13f
        val allMerchandise = MerchandiseLists()

        val weaponsList = GuiScroll(scale = tabsContainerSize.clone())
            .addKids((1..15).map {
                GuiButton(scale = scrollButtonScale.clone(), title = "Boom #$it", textSize = scrollButtonTextSize,
                    onClick = { println("clicked [Boom $it]") })
            })
        val shieldsList = GuiScroll(scale = tabsContainerSize.clone())
            .also { scrollBox ->
                scrollBox.addKids(VehicleShield.descriptor.entries
                    .filter { it.key != ShieldType.None }
                    .sortedBy { it.value.order }
                    .map { (key, value) ->
                        GuiMerchandise(scale = scrollButtonScale.clone(), name = value.name, price = value.price,
                            itemId = value.order.toString(), description = value.description, key = key.toString(),
                            onClick = { player.playerAim.setSelectedShield(key, allMerchandise, player) })
                    })
                allMerchandise.shields = scrollBox.kidElements.filterIsInstance<GuiMerchandise>()
            }
        val fuelsList = GuiScroll(scale = tabsContainerSize.clone())
            .also { scrollBox ->
                scrollBox.addKids(Fuel.descriptor.entries.sortedBy { it.value.order }
                    .map { (key, value) ->
                        GuiMerchandise(scale = scrollButtonScale.clone(), name = value.name, price = value.price,
                            itemId = value.order.toString(), description = value.description, key = key.toString(),
                            onClick = { player.playerAim.setSelectedFuel(key, allMerchandise, player) })
                    })
                allMerchandise.fuels = scrollBox.kidElements.filterIsInstance<GuiMerchandise>()
            }

        player.playerAim.setSelectedShield(null, allMerchandise, player)
        player.playerAim.setSelectedFuel(null, allMerchandise, player)

        return GuiTabs(scale = tabsContainerSize.clone(), tabsTitles = listOf("Weapons", "Shields", "Fuels"))
            .addKids(listOf(weaponsList, shieldsList, fuelsList))
            .also { it.placeOnEdge(LayoutPosition.BOTTOM_RIGHT, commandPanel.scale) }
    }

}
