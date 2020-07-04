package display.draw

import dI
import display.graphic.BasicShapes
import display.graphic.Color
import display.graphic.SnipRegion
import display.text.TextJustify
import engine.freeBody.MapBorder
import engine.freeBody.FreeBody
import engine.freeBody.Particle
import engine.freeBody.Vehicle
import engine.physics.CellLocation
import engine.physics.GravityCell
import game.GamePlayer
import game.TrajectoryPrediction
import org.jbox2d.common.Vec2
import utility.Common.getTimingFunctionEaseIn
import utility.Common.makeVec2
import utility.Common.makeVec2Circle
import utility.Common.vectorUnit
import utility.toList
import java.util.*
import kotlin.math.sqrt

class Drawer {

    val textures = dI.textures
    val renderer = dI.renderer

    fun drawDebugForces(freeBody: FreeBody) {
        //        val x = freeBody.worldBody.position.x
        //        val y = freeBody.worldBody.position.y
        //        val accelerationX = freeBody.worldBody.m_force.x
        //        val accelerationY = freeBody.worldBody.m_force.y
        //
        //        val multiplier = 2000f
        //        val linePoints = listOf(
        //            x,
        //            y,
        //            x + accelerationX * multiplier,
        //            y + accelerationY * multiplier
        //        )
        //        val triangleStripPoints = BasicShapes.getLineTriangleStrip(linePoints, .2f)
        //        val arrowHeadPoints = BasicShapes.getArrowHeadPoints(linePoints)
        //        val data = getColoredData(
        //            triangleStripPoints + arrowHeadPoints,
        //            Color(0f, 1f, 1f, 1f), Color(0f, 1f, 1f, 0.0f)
        //        ).toFloatArray()

        //        textures.getTexture(TextureEnum.white_pixel).bind()
        //        renderer.drawStrip(data)

        renderer.drawText(freeBody.id, freeBody.worldBody.position, vectorUnit, Color.WHITE, TextJustify.LEFT)
    }

    fun drawBorder(mapBorder: MapBorder) {
        textures.getTexture(mapBorder.textureConfig.texture).bind()
        renderer.drawStrip(
            mapBorder.textureConfig.gpuBufferData,
            mapBorder.worldBody.position,
            mapBorder.worldBody.angle,
            vectorUnit
        )
    }

    fun drawTrail(freeBody: FreeBody) {
        val position = freeBody.worldBody.position
        val linePoints = (listOf(position.x, position.y) + freeBody.motion.trailers.chunked(2).reversed().flatten())
        if (linePoints.size < 4) {
            return
        }
        val trailColor = when (freeBody) {
            is Vehicle -> freeBody.textureConfig.color.setAlpha(.3f)
            else -> Color(.4f, .7f, 1f, .5f)
        }
        val data = getLine(linePoints, trailColor, Color.TRANSPARENT, .1f, 0f)

        textures.getTexture(TextureEnum.white_pixel).bind()
        renderer.drawStrip(data)
    }

    fun drawFreeBody(freeBody: FreeBody) {
        textures.getTexture(freeBody.textureConfig.texture).bind()
        renderer.drawShape(
            freeBody.textureConfig.gpuBufferData,
            freeBody.worldBody.position,
            freeBody.worldBody.angle,
            vectorUnit.mul(freeBody.radius)
        )
    }

    fun drawGravityCells(gravityMap: HashMap<CellLocation, GravityCell>, resolution: Float) {
        textures.getTexture(TextureEnum.white_pixel).bind()
        val maxMass = gravityMap.maxBy { (_, cell) -> cell.totalMass }?.value?.totalMass ?: .001f
        val scale = 0.707106781f * resolution
        gravityMap.forEach { (key, cell) ->
            val data = BasicShapes.polygon4.chunked(2)
                .flatMap {
                    listOf(
                        it[0], it[1], 0f,
                        1f, 1f, 1f, sqrt(cell.totalMass / maxMass) * .9f,
                        (it[0] / 2 - 0.5f), (it[1] / 2 - 0.5f)
                    )
                }.toFloatArray()
            renderer.drawShape(data, makeVec2(key.x, key.y).mul(resolution), 0f, makeVec2(scale))
        }
    }

    fun drawBackground(textureEnum: TextureEnum,
                       textureScale: Vec2 = vectorUnit,
                       textureOffset: Vec2 = Vec2(),
                       backgroundScale: Vec2 = vectorUnit.add(Vec2(.2f, -.2f)),
                       backgroundOffset: Vec2 = Vec2()) {
        val texture = textures.getTexture(textureEnum).bind()

        val left = -texture.width / 2f
        val right = texture.width / 2f
        val top = texture.height / 2f
        val bottom = -texture.height / 2f

        val vertices = listOf(left, bottom, left, top, right, top, right, bottom).chunked(2)
        val data = vertices.flatMap {
            listOf(
                it[0], it[1], 0f,
                1f, 1f, 1f, 1f,
                (it[0] / 2f - 0.5f) * textureScale.x + textureOffset.x,
                (it[1] / 2f - 0.5f) * textureScale.y + textureOffset.y
            )
        }.toFloatArray()

        renderer.drawShape(data, backgroundOffset, scale = backgroundScale.mul(30f))
    }

    fun drawIcon(textureEnum: TextureEnum,
                 scale: Vec2 = vectorUnit,
                 offset: Vec2 = Vec2(),
                 color: Color) {
        val texture = textures.getTexture(textureEnum).bind()

        val left = -texture.width / 2f
        val right = texture.width / 2f
        val top = texture.height / 2f
        val bottom = -texture.height / 2f

        val data = listOf(left, bottom, left, top, right, top, right, bottom).chunked(2)
            .flatMap {
                listOf(
                    it[0], it[1], 0f,
                    color.red, color.green, color.blue, color.alpha,
                    (it[0] / 2f - 0.5f), (it[1] / 2f - 0.5f)
                )
            }.toFloatArray()

        renderer.drawShape(data, offset, 0f, scale, useCamera = false,
            snipRegion = SnipRegion(offset.sub(scale), scale.mul(2f)))
    }

    fun drawPlayerAimingPointer(player: GamePlayer) {
        val playerLocation = player.vehicle!!.worldBody.position
        val angle = player.playerAim.angle
        val aimLocation = makeVec2Circle(angle).mul(player.playerAim.power / 10f)

        val linePoints = listOf(
            playerLocation.x,
            playerLocation.y,
            playerLocation.x + aimLocation.x,
            playerLocation.y + aimLocation.y
        )
        val triangleStripPoints = BasicShapes.getLineTriangleStrip(linePoints, .2f)
        val arrowHeadPoints = BasicShapes.getArrowHeadPoints(linePoints, .5f)
        val data = getColoredData(triangleStripPoints + arrowHeadPoints,
            Color.RED.setAlpha(.05f), Color.RED.setAlpha(.5f)
        ).toFloatArray()

        textures.getTexture(TextureEnum.white_pixel).bind()
        renderer.drawStrip(data)
    }

    fun drawParticle(particle: Particle) {
        textures.getTexture(particle.textureConfig.texture).bind()
        renderer.drawShape(
            particle.textureConfig.gpuBufferData,
            particle.worldBody.position,
            particle.worldBody.angle,
            vectorUnit.mul(particle.radius)
        )
    }

    fun drawWarheadTrajectory(prediction: TrajectoryPrediction) {
        val color = Color("#A0505080")
        prediction.nearbyFreeBodies.forEach {
            it.textureConfig.color = color
            it.textureConfig.updateGpuBufferData()
            drawFreeBody(it)
        }

        val data = getLineTextured(prediction.warheadPath.flatMap { it.toList() },
            Color("#02eded99"), Color.TRANSPARENT, .4f, timingFunction = { step -> getTimingFunctionEaseIn(step) })

        textures.getTexture(TextureEnum.white_dot_100_pad).bind()
        renderer.drawStrip(data)
    }

    fun drawMotionPredictors(freeBody: FreeBody) {
        val worldBody = freeBody.worldBody
        val velocityUnitVector = worldBody.linearVelocity.clone().also { it.normalize() }
        val startPosition = worldBody.position.add(velocityUnitVector.mul(freeBody.radius + .1f))
        val scaledVelocity = worldBody.linearVelocity.mul(.1f)
        val points = listOf(
            startPosition,
            startPosition.add(scaledVelocity.mul(.5f)),
            startPosition.add(scaledVelocity)
        )

        val data = getLineTextured(points.flatMap { it.toList() }, Color.GREEN.setAlpha(.5f),
            startWidth = .15f,
            scale = 1.75f)

        textures.getTexture(TextureEnum.white_line_200).bind()
        renderer.drawStrip(data)
    }

    companion object {

        fun getColoredData(points: List<Float>,
                           startColor: Color = Color.WHITE,
                           endColor: Color = startColor
        ): List<Float> {
            val pointsLastIndex = points.lastIndex.toFloat() / 2f

            return points
                .chunked(2)
                .withIndex()
                .flatMap { (index, chunk) ->
                    val interpolationDistance = index.toFloat() / pointsLastIndex
                    val color = endColor * interpolationDistance + startColor * (1f - interpolationDistance)
                    listOf(
                        chunk[0], chunk[1], 0f, /* pos*/
                        color.red, color.green, color.blue, color.alpha, /* color*/
                        0f, 0f /* texture*/
                    )
                }
        }

        fun getTexturedData(points: List<Float>,
                            basePoints: List<Float>,
                            startColor: Color = Color.WHITE,
                            endColor: Color = startColor,
                            startWidth: Float,
                            endWidth: Float,
                            timingFunction: (Float) -> Float,
                            textureScale: Float = 1f): List<Float> {
            val pointsLastIndex = points.lastIndex.toFloat() / 2f

            var lastDistance = 0f
            return points
                .chunked(2)
                .withIndex()
                .zip(
                    basePoints.chunked(2)
                        .map { (x, y) -> Vec2(x, y) }
                        .windowed(2)
                        .map { (a, b) -> a.sub(b).length() * textureScale }
                        .let { listOf(0f) + it }
                        .flatMap { listOf(it, it) })
                .flatMap { (strip, distance) ->
                    val (index, location) = strip

                    val progress = index.toFloat().div(pointsLastIndex).let { timingFunction(it) }
                    val inverseProgress = 1f - progress
                    val color = endColor * progress + startColor * inverseProgress

                    val isLeftHandVertex = index.rem(2) == 0
                    val textureX = if (isLeftHandVertex) 0f else 1f

                    val scale = .5f
                    val distanceScale = (scale / endWidth) * progress + (scale / startWidth) * inverseProgress

                    if (isLeftHandVertex) {
                        lastDistance += distance
                    }
                    val textureY = lastDistance * distanceScale

                    listOf(
                        location[0], location[1], 0f, /* pos*/
                        color.red, color.green, color.blue, color.alpha, /* color*/
                        textureX, textureY /* texture*/
                    )
                }
        }

        fun getLine(points: List<Float>,
                    startColor: Color = Color.WHITE,
                    endColor: Color = startColor,
                    startWidth: Float = 1f,
                    endWidth: Float = startWidth,
                    wrapAround: Boolean = false
        ): FloatArray {
            //            if (points.size < 3) {
            //                return floatArrayOf()
            //            }
            val triangleStripPoints = BasicShapes.getLineTriangleStrip(points, startWidth, endWidth, wrapAround)
            return getColoredData(triangleStripPoints, startColor, endColor).toFloatArray()
        }

        fun getLineTextured(points: List<Float>,
                            startColor: Color = Color.WHITE,
                            endColor: Color = startColor,
                            startWidth: Float = 1f,
                            endWidth: Float = startWidth,
                            wrapAround: Boolean = false,
                            timingFunction: (Float) -> Float = { input -> input },
                            scale: Float = 1f): FloatArray {
            if (points.size < 3) {
                return floatArrayOf()
            }
            val triangleStripPoints = BasicShapes.getLineTriangleStrip(points, startWidth, endWidth, wrapAround)
            return getTexturedData(triangleStripPoints, points, startColor, endColor, startWidth, endWidth,
                timingFunction = timingFunction, textureScale = scale)
                .toFloatArray()
        }

    }

}
