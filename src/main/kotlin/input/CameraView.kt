package input

import dI
import engine.freeBody.FreeBody
import engine.freeBody.Vehicle
import io.reactivex.subjects.PublishSubject
import org.jbox2d.common.Vec2
import org.joml.*
import utility.PidController
import utility.PidControllerVec2
import utility.StopWatch
import utility.math.clone
import kotlin.math.absoluteValue

class CameraView {

    val windowWidth: Float
        get() = dI.window.width.toFloat()
    val windowHeight: Float
        get() = dI.window.height.toFloat()

    val windowWidthInt: Int
        get() = dI.window.width
    val windowHeightInt: Int
        get() = dI.window.height

    var renderCamera = Matrix4f()

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

        val rescale = .015f //.002f
        renderCamera = Matrix4f()
            .scale(rescale)
            .translate(-location.x, -location.y, z)
            .transpose()
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
        targetZ = (targetZ + movement * 4f).coerceIn(minZoom, maxZoom)
    }

    fun reset() {
        targetLocation = Vec2()
        targetZ = defaultZoom
        targetVelocity = Vec2()
        targetVelocityAverage = Vec2()
        stopWatch.reset()
        isFollowFunctionMode = false
    }

    fun getScreenLocation(cursorLocation: Vec2): Vec2 = cursorLocation
        .sub(Vec2(windowWidth, windowHeight).mul(.5f))
        .also { it.y *= -1f }

    fun getWorldLocation(pixelLocation: Vec2): Vec2 {
        val view = renderCamera.clone().transpose()
        val projection = dI.renderer.projectionGameWorld.clone().transpose()
        val adj = pixelLocation.let { Vec2(it.x, windowHeight - it.y) }

        val getOrigin = { Vector3f(location.x, location.y, -z) }
        val origin = getOrigin()
        val direction = Vector3f()
        projection.mul(view)
            .unprojectRay(adj.x, adj.y, intArrayOf(0, 0, windowWidthInt, windowHeightInt), origin, direction)

        getOrigin().also { origin.set(it.x, it.y, it.z) }
        val ray = Rayf(origin, direction)
        val plane = Planef(Vector3f(0f, 0f, 0f), Vector3f(0f, 0f, 1f))
        val t = Intersectionf.intersectRayPlane(ray, plane, .01f)

        val hit = Vec2(ray.oX, ray.oY).add(Vec2(ray.dX, ray.dY).mul(t))
        return hit
    }

    fun getGuiLocation(worldLocation: Vec2): Vec2 {
        val view = renderCamera.clone().transpose()
        val projection = dI.renderer.projectionGameWorld.clone().transpose()

        val windowLocation = projection.mul(view).project(
            worldLocation.x, worldLocation.y, 0f,
            intArrayOf(0, 0, windowWidthInt, windowHeightInt), Vector3f())

        return getScreenLocation(Vec2(windowLocation.x, windowHeight - windowLocation.y))
    }

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
            maxDistance.times(.003f).coerceIn(minZoom, maxZoom * .9f + minZoom * .1f)
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
        const val defaultZoom = -60f
        const val maxZoom = -20f
        const val minZoom = -90f

    }

}
