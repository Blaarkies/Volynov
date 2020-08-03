package input

import dI
import engine.freeBody.FreeBody
import engine.freeBody.Vehicle
import io.reactivex.subjects.PublishSubject
import org.jbox2d.common.Vec2
import utility.PidController
import utility.PidControllerVec2
import utility.StopWatch
import utility.math.Matrix4f
import kotlin.math.absoluteValue
import kotlin.math.pow

class CameraView {

    val windowWidth: Float
        get() = dI.window.width.toFloat()
    val windowHeight: Float
        get() = dI.window.height.toFloat()

    val windowWidthInt: Int
        get() = dI.window.width
    val windowHeightInt: Int
        get() = dI.window.height

    var location = Vec2()
    var z = defaultZoom

    private var targetZ = z
    private val zoomController = PidController(-.06f, -.0001f, -.06f)

    private var targetVelocity = Vec2()
    private var targetVelocityAverage = Vec2()
    private var targetLocation = location
    private val normalMovementController = PidControllerVec2(-.06f, -.00025f, -.01f)
    private val slowMovementController = PidControllerVec2(-.02f, 0f, -.01f)

    private val stopWatch = StopWatch()

    private var isFollowFunctionMode = false
    private var lastFunctionLocation: () -> Vec2 = { targetLocation }
    private var lastFunctionZoom: () -> Float = { targetZ }

    val unsubscribeCheckCameraEvent = PublishSubject.create<Boolean>()

    fun update() {
        when {
            isFollowFunctionMode -> processPidInputs(lastFunctionZoom(), lastFunctionLocation())
            else -> processPidInputs(targetZ, targetLocation)
        }
    }

    private fun processPidInputs(targetZ: Float, targetLocation: Vec2) {
        if (z.minus(targetZ).absoluteValue > .0001f) {
            z += zoomController.getReaction(z, targetZ)
        }
        val newTargetLocation = targetLocation.add(targetVelocityAverage.mul(.7f))
        if (location.sub(newTargetLocation).length() > .01f) {
            when {
                stopWatch.elapsedTime < 130f -> location.addLocal(
                    slowMovementController.getReaction(location, newTargetLocation))
                else -> location.addLocal(normalMovementController.getReaction(location, newTargetLocation))
            }
        }
        targetVelocityAverage = targetVelocityAverage.mul(oldPortion).add(targetVelocity.mul(newPortion))
    }

    fun setNewLocation(position: Vec2) {
        targetLocation = position
        targetVelocity = Vec2()
    }

    fun setNewZoom(zoom: Float) {
        targetZ = zoom
    }

    fun trackFreeBody(newFreeBody: FreeBody, lead: Boolean = true) {
        targetLocation = newFreeBody.worldBody.position
        targetVelocity = if (lead) newFreeBody.worldBody.linearVelocity else Vec2()
        targetVelocityAverage = targetVelocity.clone()
        stopWatch.reset()
        isFollowFunctionMode = false
    }

    fun moveLocation(movement: Vec2) {
        location.addLocal(movement)
        targetLocation = location
        targetVelocity = Vec2()
        targetVelocityAverage = Vec2()
        normalMovementController.reset()
        slowMovementController.reset()
        isFollowFunctionMode = false
    }

    fun moveZoom(movement: Float) {
        targetZ = (targetZ + movement * targetZ.pow(1.2f) * 50f).coerceIn(maxZoom, minZoom)
    }

    fun reset() {
        targetLocation = Vec2()
        targetZ = .05f
        targetVelocity = Vec2()
        targetVelocityAverage = Vec2()
        stopWatch.reset()
        isFollowFunctionMode = false
    }

    fun getRenderCamera(): Matrix4f {
        val zoomScale = 1f / z
        return Matrix4f.scale(zoomScale, zoomScale, 1f)
            .multiply(Matrix4f.translate(-location.x, -location.y, 0f))
    }

    fun getScreenLocation(cursorLocation: Vec2): Vec2 = cursorLocation
        .sub(Vec2(windowWidth, windowHeight).mul(.5f))
        .also { it.y *= -1f }

    fun getWorldLocation(screenLocation: Vec2): Vec2 = getScreenLocation(screenLocation).mul(z).add(location)

    fun getGuiLocation(worldLocation: Vec2): Vec2 = worldLocation.sub(location).mul(1f / z)

    fun checkCameraEvent() {
        val otherWarhead = dI.gameState.warheads.firstOrNull()
        if (otherWarhead != null) {
            trackFreeBody(otherWarhead)
            return
        }

        val damagedVehicles = dI.gamePhaseHandler.damagedVehicles
        when {
            damagedVehicles.size == 1 -> {
                trackFreeBody(damagedVehicles.first())
                return
            }
            damagedVehicles.size > 1 -> {
                followManyVehicles(damagedVehicles)
                return
            }
        }

        val floatingVehicle = dI.gameState.vehicles.firstOrNull { !it.isStable }
        if (floatingVehicle != null) {
            trackFreeBody(floatingVehicle)
            return
        }

        trackFreeBody(dI.gameState.vehicles.random())
    }

    private fun followManyVehicles(damagedVehicles: List<Vehicle>) {
        val averageLocation = {
            damagedVehicles.map { it.worldBody.position }
                .reduce { sum, location -> sum.add(location) }
                .mulLocal(1f / damagedVehicles.size.toFloat())
        }
        val minimumZoom = {
            val cameraCenter = averageLocation()
            val maxDistance = damagedVehicles
                .map { it.worldBody.position.sub(cameraCenter).length() }
                .max() ?: 1f
            maxDistance.times(.003f).coerceIn(maxZoom * .9f + minZoom * .1f, minZoom)
        }

        followFunctionLocation(averageLocation, minimumZoom)
    }

    private fun followFunctionLocation(getLocation: () -> Vec2, getZoom: () -> Float) {
        lastFunctionLocation = getLocation
        lastFunctionZoom = getZoom
        isFollowFunctionMode = true
        stopWatch.reset()
    }

    companion object {

        const val newPortion = .05f
        const val oldPortion = 1f - newPortion
        const val defaultZoom = .05f
        const val maxZoom = .01f
        const val minZoom = .15f

    }

}
