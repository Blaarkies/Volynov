package game

import display.KeyboardEvent
import display.Window
import display.draw.Drawer
import display.draw.TextureEnum
import display.events.MouseButtonEvent
import display.graphic.Color
import display.gui.GuiController
import engine.GameState
import engine.motion.Director
import engine.shields.VehicleShield
import kotlinx.coroutines.yield
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.contacts.ContactEdge
import utility.Common.getTimingFunctionEaseIn
import utility.Common.getTimingFunctionEaseOut
import utility.Common.vectorUnit
import kotlin.math.roundToInt

class GamePhaseHandler(private val gameState: GameState, val drawer: Drawer) {

    private val timeStep = 1f / 60f
    private val velocityIterations = 8
    private val positionIterations = 3

    private val camera
        get() = gameState.camera

    private var currentPhase = GamePhases.NONE
    private var isTransitioning = false

    private val currentTime
        get() = System.currentTimeMillis()

    private val elapsedTime
        get() = (currentTime - lastPhaseTimestamp)

    private var lastPhaseTimestamp = currentTime

    private val guiController = GuiController(drawer)
    private lateinit var exitCall: () -> Unit

    fun init(window: Window) {
        exitCall = { window.exit() }

        setupMainMenu()

//                currentPhase = GamePhases.PLAYERS_PICK_SHIELDS
//                isTransitioning = false
//                gameState.gamePlayers.addAll((1..3).map { GamePlayer(it.toString()) })
//                MapGenerator.populateNewGameMap(gameState)
//                gameState.gamePlayers.forEach { it.vehicle?.shield = VehicleShield() }
//                gameState.playerOnTurn = gameState.gamePlayers[0]
//
//                setupNextPlayersTurn()
    }

    fun dragMouseRightClick(movement: Vec2) {
        camera.moveLocation(movement.mulLocal(-camera.z))
    }

    fun scrollCamera(movement: Float) {
        camera.moveZoom(movement * -.001f)
    }

    fun pauseGame(event: KeyboardEvent) {
        currentPhase = when (currentPhase) {
            GamePhases.PAUSE -> GamePhases.PLAY
            GamePhases.PLAY -> GamePhases.PAUSE
            else -> GamePhases.PAUSE
        }
        startTransition()
    }

    private fun startTransition() {
        lastPhaseTimestamp = currentTime
        isTransitioning = true
    }

    private fun startNewPhase(newPhase: GamePhases) {
        currentPhase = newPhase
        startTransition()
    }

    fun update() {
        camera.update()

        val cp = currentPhase
        when {
            cp == GamePhases.PAUSE && isTransitioning -> tickGamePausing()
            cp == GamePhases.PAUSE -> return
            cp == GamePhases.PLAY && isTransitioning -> tickGameUnpausing()
            cp == GamePhases.MAIN_MENU -> return
            cp == GamePhases.MAIN_MENU_SELECT_PLAYERS -> return
            cp == GamePhases.NEW_GAME_INTRO && isTransitioning -> tickGameUnpausing()
            cp == GamePhases.NEW_GAME_INTRO -> handleIntro()
            cp == GamePhases.PLAYERS_PICK_SHIELDS && isTransitioning -> {
                if (elapsedTime > pauseTime) isTransitioning = false
            }
            cp == GamePhases.PLAYERS_PICK_SHIELDS -> return
            cp == GamePhases.PLAYERS_TURN -> return
            cp == GamePhases.PLAYERS_TURN_FIRED && isTransitioning -> tickGameUnpausing(quickStartTime)
            cp == GamePhases.PLAYERS_TURN_FIRED -> handlePlayerShot()
            cp == GamePhases.PLAYERS_TURN_FIRED_ENDS_EARLY -> handlePlayerShotEndsEarly()
            cp == GamePhases.PLAYERS_TURN_AIMING -> return
            cp == GamePhases.PLAYERS_TURN_POWERING -> return
            cp == GamePhases.END_ROUND && isTransitioning -> tickGamePausing(outroDuration, endSpeed = .1f)
            cp == GamePhases.END_ROUND -> gameState.tickClock(timeStep * .1f, velocityIterations, positionIterations)

            else -> gameState.tickClock(timeStep, velocityIterations, positionIterations)
        }
    }

    fun render() {
        when (currentPhase) {
            GamePhases.MAIN_MENU -> guiController.render()
            GamePhases.MAIN_MENU_SELECT_PLAYERS -> guiController.render()
            GamePhases.PLAYERS_PICK_SHIELDS -> drawWorldAndGui()
            GamePhases.PLAYERS_TURN -> drawWorldAndGui()
            GamePhases.PLAYERS_TURN_AIMING -> {
                drawWorldAndGui()
                drawer.drawPlayerAimingPointer(gameState.playerOnTurn!!)
            }
            GamePhases.PLAYERS_TURN_POWERING -> {
                drawWorldAndGui()
                drawer.drawPlayerAimingPointer(gameState.playerOnTurn!!)
            }
            GamePhases.END_ROUND -> drawWorldAndGui()
            else -> drawPlayPhase()
        }

        drawer.renderer.drawText(
            "Animating: ${isTransitioning.toString().padEnd(5, ' ')} ${currentPhase.name}",
            Vec2(120f - camera.windowWidth * .5f, -10f + camera.windowHeight * .5f),
            vectorUnit.mul(0.1f), Color.GREEN, false
        )

        drawer.renderer.drawText(
            "${elapsedTime.div(100f).roundToInt().div(10f)} seconds",
            Vec2(40f - camera.windowWidth * .5f, -30f + camera.windowHeight * .5f),
            vectorUnit.mul(0.1f), Color.GREEN, false
        )
    }

    private fun handlePlayerShotEndsEarly() {
        when {
            isTransitioning -> tickGamePausing()
            else -> setupNextPlayersTurn()
        }
    }

    private fun handlePlayerShot() {
        val roundEndsEarly = (gameState.warheads.none()
                && gameState.particles.none()
                && gameState.vehicles
            .all {
                it.worldBody.contactList != null
                        && getContactBodies(it.worldBody.contactList).any { other -> other.mass > 50f }
            })
        when {
            roundEndsEarly -> if (!checkStateEndOfRound()) startNewPhase(GamePhases.PLAYERS_TURN_FIRED_ENDS_EARLY)
            elapsedTime > maxTurnDuration -> setupNextPlayersTurn()
            elapsedTime > (maxTurnDuration - pauseTime) -> tickGamePausing(
                pauseTime, calculatedElapsedTime = (elapsedTime - maxTurnDuration + pauseTime)
            )
            else -> gameState.tickClock(timeStep, velocityIterations, positionIterations)
        }

    }

    private fun getContactBodies(contactEdge: ContactEdge): Sequence<Body> = sequence {
        var currentContact = contactEdge
        yield(currentContact.other)

        while (currentContact.next != null) {
            yield(currentContact.other)
            currentContact = currentContact.next
        }
    }

    private fun handleIntro() {
        when {
            elapsedTime > introDuration -> playerSelectsShield()
            elapsedTime > (introDuration - introStartSlowdown) -> tickGamePausing(
                introStartSlowdown, calculatedElapsedTime = (elapsedTime - introDuration + introStartSlowdown)
            )
            else -> gameState.tickClock(timeStep, velocityIterations, positionIterations)
        }
    }

    private fun playerSelectsShield(player: GamePlayer? = null) {
        player?.vehicle?.shield = VehicleShield()

        if (gameState.gamePlayers.all { it.vehicle?.shield != null }) {
            setupNextPlayersTurn()
            return
        }

        setNextPlayerOnTurn()
        setupPlayersPickShields()

        currentPhase = GamePhases.PLAYERS_PICK_SHIELDS
        //        startTransition()
    }

    private fun setupNextPlayersTurn() {
        if (checkStateEndOfRound()) {
            return
        }

        gameState.gamePlayers.map { "Player ${it.name} HP:${it.vehicle!!.hitPoints.toInt()}" }
            .joinToString().let { println(it) }

        setNextPlayerOnTurn()
        setupPlayerCommandPanel()

        startNewPhase(GamePhases.PLAYERS_TURN)
    }

    private fun checkStateEndOfRound(): Boolean {
        val vehiclesDestroyed = gameState.gamePlayers.count { it.vehicle!!.hitPoints > 0 } < 2
        if (vehiclesDestroyed) {
            startNewPhase(GamePhases.END_ROUND)
            guiController.createRoundLeaderboard(gameState.gamePlayers, onClickNextRound = {})
            return true
        }
        return false
    }

    private fun setupPlayerCommandPanel() {
        guiController.createPlayerCommandPanel(
            player = gameState.playerOnTurn!!,
            onClickAim = { startNewPhase(GamePhases.PLAYERS_TURN_AIMING) },
            onClickPower = { startNewPhase(GamePhases.PLAYERS_TURN_POWERING) },
            onClickFire = { player -> playerFires(player) }
        )
    }

    private fun playerFires(player: GamePlayer) {
        // check() {} player has enough funds && in stable position to fire large warheads

        val firedWarhead = gameState.fireWarhead(player, "boom small")
        camera.trackFreeBody(firedWarhead, 200f)

        startNewPhase(GamePhases.PLAYERS_TURN_FIRED)
    }

    private fun setNextPlayerOnTurn() {
        checkNotNull(gameState.playerOnTurn) { "No player is on turn." }
        val playerOnTurn = gameState.playerOnTurn!!
        val players = gameState.gamePlayers.filter { it.vehicle!!.hitPoints > 0 }
        gameState.playerOnTurn = players[(players.indexOf(playerOnTurn) + 1).rem(players.size)]

        camera.trackFreeBody(gameState.playerOnTurn!!.vehicle!!)
    }

    private fun setupPlayersPickShields() {
        guiController.createPlayersPickShields(
            onClickShield = { player -> playerSelectsShield(player) },
            player = gameState.playerOnTurn!!
        )
    }

    private fun drawWorldAndGui() {
        drawPlayPhase()
        guiController.render()
    }

    private fun drawPlayPhase() {
        drawer.drawPicture(TextureEnum.stars_2k)

        val allFreeBodies = gameState.gravityBodies
        allFreeBodies.forEach { drawer.drawTrail(it) }
        gameState.particles.forEach { drawer.drawParticle(it) }
        allFreeBodies.forEach { drawer.drawFreeBody(it) }
        //        allFreeBodies.forEach { drawDebugForces(it) }
        //        drawer.drawGravityCells(gameState.gravityMap, gameState.resolution)
    }

    private fun tickGameUnpausing(
        duration: Float = pauseTime,
        endPhase: GamePhases? = null,
        calculatedElapsedTime: Float? = null
    ) {
        val interpolateStep = (calculatedElapsedTime ?: elapsedTime.toFloat()) / duration
        if (interpolateStep >= 1f) {
            currentPhase = endPhase ?: currentPhase
            isTransitioning = false
        } else {
            val timeFunctionStep = getTimingFunctionEaseOut(interpolateStep)
            gameState.tickClock(timeStep * timeFunctionStep, velocityIterations, positionIterations)
        }
    }

    private fun tickGamePausing(
        duration: Float = pauseTime,
        endPhase: GamePhases? = null,
        calculatedElapsedTime: Float? = null,
        endSpeed: Float = 0f
    ) {
        val interpolateStep = (calculatedElapsedTime ?: elapsedTime.toFloat()) / duration
        if (interpolateStep >= 1f) {
            currentPhase = endPhase ?: currentPhase
            isTransitioning = false
        } else {
            val timeFunctionStep = getTimingFunctionEaseIn(1f - interpolateStep) * (1f - endSpeed) + endSpeed
            gameState.tickClock(timeStep * timeFunctionStep, velocityIterations, positionIterations)
            //            println("${roundFloat(interpolateStep, 2).toString().padEnd(4, '0')} <> " + roundFloat(timeFunctionStep, 2).toString().padEnd(4, '0'))
        }
    }

    fun doubleLeftClick(location: Vec2, click: MouseButtonEvent) {
        val transformedLocation = getScreenLocation(location).mul(camera.z).add(camera.location)

        val clickedBody = gameState.gravityBodies.find {
            it.worldBody.position
                .add(transformedLocation.mul(-1f))
                .length() <= it.radius
        }
            ?: return

        camera.trackFreeBody(clickedBody)
    }

    fun keyPressArrowLeft(event: KeyboardEvent) {
        drawer.renderer.debugOffset.addLocal(Vec2(-0.005f, 0f))
    }

    fun keyPressArrowRight(event: KeyboardEvent) {
        drawer.renderer.debugOffset.addLocal(Vec2(0.005f, 0f))
    }

    fun moveMouse(location: Vec2) {
        when {
            mouseElementPhases.any { currentPhase == it } -> guiController.checkHover(getScreenLocation(location))
            currentPhase == GamePhases.PLAYERS_TURN_AIMING -> {
                val (playerOnTurn, transformedLocation, playerLocation) = getPlayerAndMouseLocations(location)
                val aimDirection = Director.getDirection(
                    transformedLocation.x, transformedLocation.y, playerLocation.x, playerLocation.y
                )
                playerOnTurn.playerAim.angle = aimDirection
                guiController.update()
            }
            currentPhase == GamePhases.PLAYERS_TURN_POWERING -> {
                val (playerOnTurn, transformedLocation, playerLocation) = getPlayerAndMouseLocations(location)
                val distance = Director.getDistance(
                    transformedLocation.x, transformedLocation.y, playerLocation.x, playerLocation.y
                )
                playerOnTurn.playerAim.power = (distance - 1f) * 10f
                guiController.update()
            }
        }

    }

    private fun getPlayerAndMouseLocations(location: Vec2): Triple<GamePlayer, Vec2, Vec2> {
        checkNotNull(gameState.playerOnTurn) { "No player is on turn." }
        val playerOnTurn = gameState.playerOnTurn!!
        val transformedLocation = getScreenLocation(location).mul(camera.z).add(camera.location)
        val playerLocation = playerOnTurn.vehicle!!.worldBody.position
        return Triple(playerOnTurn, transformedLocation, playerLocation)
    }

    fun leftClickMouse(event: MouseButtonEvent) {
        when {
            mouseElementPhases.any { currentPhase == it } -> guiController.checkLeftClick(
                getScreenLocation(event.location))
            currentPhase == GamePhases.PLAYERS_TURN_AIMING -> currentPhase = GamePhases.PLAYERS_TURN
            currentPhase == GamePhases.PLAYERS_TURN_POWERING -> currentPhase = GamePhases.PLAYERS_TURN
        }
    }

    private fun getScreenLocation(location: Vec2): Vec2 =
        location.add(Vec2(-camera.windowWidth * .5f, -camera.windowHeight * .5f))
            .let {
                it.y *= -1f
                it
            }

    fun keyPressEscape(event: KeyboardEvent) {
        when (currentPhase) {
            GamePhases.MAIN_MENU -> exitCall()
            else -> setupMainMenu()
        }
    }

    private fun setupMainMenu() {
        currentPhase = GamePhases.MAIN_MENU
        gameState.reset()
        guiController.createMainMenu(
            onClickNewGame = { setupMainMenuSelectPlayers() },
            onClickSettings = {},
            onClickQuit = exitCall
        )
    }

    private fun setupMainMenuSelectPlayers() {
        currentPhase = GamePhases.MAIN_MENU_SELECT_PLAYERS
        gameState.reset()
        guiController.createMainMenuSelectPlayers(
            onClickStart = { setupStartGame() },
            onClickCancel = { setupMainMenu() },
            onAddPlayer = { onAddPlayerButton() },
            onRemovePlayer = { onRemovePlayerButton() },
            playerList = gameState.gamePlayers
        )
    }

    private fun onAddPlayerButton() {
        gameState.gamePlayers.add(GamePlayer((gameState.gamePlayers.size + 1).toString()))
        guiController.updateMainMenuSelectPlayers(gameState.gamePlayers, { onAddPlayerButton() },
            { onRemovePlayerButton() })
    }

    private fun onRemovePlayerButton() {
        gameState.gamePlayers.remove(gameState.gamePlayers.last())
        guiController.updateMainMenuSelectPlayers(gameState.gamePlayers, { onAddPlayerButton() },
            { onRemovePlayerButton() })
    }

    private fun setupStartGame() {
        guiController.clear()
        startNewPhase(GamePhases.NEW_GAME_INTRO)

        MapGenerator.populateNewGameMap(gameState)

        check(gameState.gamePlayers.size > 0) { "Cannot play a game with no players." }
        gameState.playerOnTurn = gameState.gamePlayers.random()
    }

    companion object {

        private const val pauseTime = 1000f
        private const val introDuration = 3500f
        private const val introStartSlowdown = 2000f
        private const val maxTurnDuration = 30000f
        private const val quickStartTime = 300f
        private const val outroDuration = 5000f

        private val mouseElementPhases = listOf(
            GamePhases.MAIN_MENU,
            GamePhases.MAIN_MENU_SELECT_PLAYERS,
            GamePhases.PLAYERS_PICK_SHIELDS,
            GamePhases.PLAYERS_TURN,
            GamePhases.END_ROUND
        )

    }

}