package display.gui.special

import dI
import display.draw.TextureEnum
import display.gui.LayoutController
import display.gui.LayoutPosition
import display.gui.base.GuiElement
import display.gui.base.HasLabel
import display.gui.elements.*
import display.text.TextJustify
import game.GamePlayer
import game.fuel.Fuel
import org.jbox2d.common.Vec2
import utility.Common
import utility.Common.makeVec2
import kotlin.math.ceil

class GuiCommandPanel(player: GamePlayer,
                      onClickAim: (player: GamePlayer) -> Unit,
                      onClickPower: (player: GamePlayer) -> Unit,
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
        val actionButtons = getActionButtons(player, onClickAim, onClickPower, onClickMove, onClickFire)
        val aimingInfo = getAimSetter(this, player)
        val playerStats = getPlayerStats(player, this)
        val shoppingCart = getShoppingCart(player)

        addKids(actionButtons + aimingInfo + playerStats + tabs + shoppingCart +
                GuiLabel(Vec2(-90f, 130f),
                    title = "Weapons/Shields not implemented", textSize = .11f))
    }

    private fun getShoppingCart(player: GamePlayer): List<GuiLabel> {
        return listOf(
            GuiLabel(title = "Selected Equipment", textSize = .1f),
            GuiLabel(title = "Weapon", textSize = .1f),
            GuiLabel(title = "Shield", textSize = .1f),
            GuiLabel(textSize = .1f, updateCallback = { e ->
                (e as GuiLabel).title = "Fuel      ${
                Fuel.descriptor[player.playerAim.selectedFuel]?.name ?: ""}"
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

    private fun getPlayerStats(player: GamePlayer, commandPanel: GuiPanel): List<GuiLabel> {
        val hp = ceil(player.vehicle!!.hitPoints).toInt()
        val energy = ceil(player.vehicle!!.shield!!.energy).toInt()
        val cash = player.cash.toInt()

        return listOf(
            GuiLabel(title = "HP      $hp%", textSize = .12f),
            GuiLabel(title = "Energy ${energy}", textSize = .12f),
            GuiLabel(title = "Cash    ${cash}μ", textSize = .12f))
            .also { labels ->
                labels.forEach { it.updateScale(Vec2(1f, 10f)) }
                val first = labels.first()
                first.placeOnEdge(LayoutPosition.TOP_LEFT, scale, first.scale.add(makeVec2(10f)))
                labels.drop(1).forEach { it.addOffset(Vec2(first.offset.x, 0f)) }
                LayoutController.setElementsInRows(labels, centered = false)
            }
    }

    private fun getAimSetter(commandPanel: GuiPanel, player: GamePlayer): List<GuiElement> {
//        val iconScale = makeVec2(20)
//        val textSize = .15f
//        val iconPadding = makeVec2(5)
//        val iconAim = GuiIcon(scale = iconScale, texture = TextureEnum.icon_aim_direction, padding = iconPadding)
//            .also {
//                it.updateOffset(
//                    LayoutController.getOffsetForLayoutPosition(LayoutPosition.CENTER_LEFT, commandPanel.scale,
//                        it.scale))
//            }
//        val iconPower = GuiIcon(iconAim.offset.clone(), iconScale,
//            texture = TextureEnum.icon_aim_power, padding = iconPadding)
//
//        return listOf(iconAim, iconPower)
//            .also { icons -> LayoutController.setElementsInRows(icons, centered = false) }
//            .zip(listOf(
//                GuiLabel(Vec2(), TextJustify.LEFT, getPlayerAimAngleDisplay(player), textSize,
//                    updateCallback = { (it as HasLabel).title = getPlayerAimAngleDisplay(player) })
//                    .also { it.scale.set(makeVec2(2f)) },
//                GuiLabel(Vec2(), TextJustify.LEFT, getPlayerAimPowerDisplay(player), .15f,
//                    updateCallback = { (it as HasLabel).title = getPlayerAimPowerDisplay(player) })
//                    .also { it.scale.set(makeVec2(2f)) }
//            ))
//            .also {
//                it.forEach { (icon, label) ->
//                    label.updateOffset(icon.offset)
//                    LayoutController.setElementsInColumns(listOf(icon, label), centered = false)
//                }
//            }
//            .flatMap { it.toList() }

        return listOf(
            GuiSpinner(Vec2()).also {
                it.placeOnEdge(LayoutPosition.CENTER_LEFT, scale, makeVec2(74f))
                it.addOffset(Vec2(0f, 90f))
            }
        )
    }

    private fun getPlayerAimAngleDisplay(player: GamePlayer): String =
        player.playerAim.getDegreesAngle().let { displayNumber(it, 2) + "º" }

    private fun getPlayerAimPowerDisplay(player: GamePlayer): String =
        player.playerAim.power.let { displayNumber(it, 2) + "%" }

    private fun displayNumber(value: Float, decimals: Int): String =
        Common.roundFloat(value, decimals).toString()

    private fun getActionButtons(player: GamePlayer,
                                 onClickAim: (player: GamePlayer) -> Unit,
                                 onClickPower: (player: GamePlayer) -> Unit,
                                 onClickMove: (player: GamePlayer) -> Unit,
                                 onClickFire: (player: GamePlayer) -> Unit): List<GuiButton> {
        val actionButtonScale = Vec2(50f, 25f) // dragHandleScale = Vec2(90f, 25f)
        return listOf(
            // GuiButton(actionButtonsOffset.clone(), actionButtonScale, title = "Aim", textSize = .12f, onClick = { onClickAim(player) }),
            // GuiButton(actionButtonsOffset.clone(), actionButtonScale, title = "Power", textSize = .12f, onClick = { onClickPower(player) }),
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
        val weaponsList = GuiScroll(scale = tabsContainerSize.clone())
            .addKids((1..15).map {
                GuiButton(scale = scrollButtonScale.clone(), title = "Boom #$it", textSize = scrollButtonTextSize,
                    onClick = { println("clicked [Boom $it]") })
            })
        val shieldsList = GuiScroll(scale = tabsContainerSize.clone())
            .addKids((1..5).map {
                GuiButton(scale = scrollButtonScale.clone(), title = "Shield #$it", textSize = scrollButtonTextSize,
                    onClick = { println("clicked [Shield $it]") })
            })
        val fuelsList = GuiScroll(scale = tabsContainerSize.clone())
            .also { scrollBox ->
                scrollBox.addKids(Fuel.descriptor.entries.sortedBy { it.value.order }
                    .map { (key, value) ->
                        GuiMerchandise(scale = scrollButtonScale.clone(),
                            name = value.name, price = value.price, itemId = value.order.toString(),
                            description = value.description,
                            onClick = {
                                player.playerAim.setSelectedFuel(
                                    key, scrollBox.kidElements.filterIsInstance<GuiMerchandise>(), player)
                            })
                    })
                player.playerAim.setSelectedFuel(null, scrollBox.kidElements.filterIsInstance<GuiMerchandise>(), player)
            }

        return GuiTabs(scale = tabsContainerSize.clone(), tabsTitles = listOf("Weapons", "Shields", "Fuels"))
            .addKids(listOf(weaponsList, shieldsList, fuelsList))
            .also { it.placeOnEdge(LayoutPosition.BOTTOM_RIGHT, commandPanel.scale) }
    }

}
