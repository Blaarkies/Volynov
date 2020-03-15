package display.draw

import display.graphic.BasicShapes
import display.graphic.Color
import display.graphic.Renderer
import display.graphic.Texture
import engine.freeBody.FreeBody
import engine.physics.CellLocation
import engine.physics.GravityCell
import org.jbox2d.common.Vec2
import java.util.*
import kotlin.math.PI
import kotlin.math.sqrt

class Drawer(private val renderer: Renderer, private val textures: TextureHolder) {

    fun drawDebugForces(freeBody: FreeBody) {
        val x = freeBody.worldBody.position.x
        val y = freeBody.worldBody.position.y
        val accelerationX = freeBody.worldBody.m_force.x
        val accelerationY = freeBody.worldBody.m_force.y

        val multiplier = 2000f
        val linePoints = listOf(
            x,
            y,
            x + accelerationX * multiplier,
            y + accelerationY * multiplier
        )
        val triangleStripPoints = BasicShapes.getTriangleStripPoints(linePoints, 2f)
        val arrowHeadPoints = BasicShapes.getArrowHeadPoints(linePoints)
        val data = getColoredData(
            triangleStripPoints + arrowHeadPoints,
            Color(0f, 1f, 1f, 1f), Color(0f, 1f, 1f, 0.0f)
        ).toFloatArray()

        textures.white_pixel.bind()
//        renderer.drawStrip(data)

        renderer.drawText(freeBody.id, x, y, Color.WHITE)
    }

    fun drawTrail(freeBody: FreeBody) {
        val linePoints = (freeBody.motion.trailers + listOf(freeBody.worldBody.position.x, freeBody.worldBody.position.y))
            .chunked(2)
            .chunked(2)
            .filter { it.size > 1 }
            .flatMap {
                val (a, b) = it
                listOf(a[0], a[1], b[0], b[1])
            }
        val data = getLineFromPoints(linePoints, Color(0.4f, 0.7f, 1f, 0.5f), Color.TRANSPARENT, .1f, 0f)

        textures.white_pixel.bind()
        renderer.drawStrip(data)
    }

    private fun getLineFromPoints(
        points: List<Float>,
        startColor: Color = Color.WHITE,
        endColor: Color = startColor,
        startWidth: Float = 1f,
        endWidth: Float = startWidth
    ): FloatArray {
        val triangleStripPoints = BasicShapes.getTriangleStripPoints(points, startWidth, endWidth)
        return getColoredData(triangleStripPoints, startColor, endColor).toFloatArray()
    }

    fun drawFreeBody(freeBody: FreeBody) {
        freeBody.textureConfig.texture.bind()
        renderer.drawShape(
            freeBody.textureConfig.gpuBufferData,
            freeBody.worldBody.position.x,
            freeBody.worldBody.position.y,
            freeBody.worldBody.angle,
            freeBody.radius,
            freeBody.radius
        )
    }

    private fun getColoredData(
        points: List<Float>,
        startColor: Color = Color.WHITE,
        endColor: Color = startColor
    ): List<Float> {
        val pointsLastIndex = points.lastIndex.toFloat() / 2f

        return points
            .chunked(2)
            .withIndex()
            .flatMap { (index, chunk) ->
                val interpolationDistance = index.toFloat() / pointsLastIndex
                val color = startColor * interpolationDistance + endColor * (1f - interpolationDistance)
                listOf(
                    chunk[0],
                    chunk[1],
                    0f, /* pos*/
                    color.red,
                    color.green,
                    color.blue,
                    color.alpha, /* color*/
                    0f,
                    0f /* texture*/
                )
            }
    }

    fun drawGravityCells(gravityMap: HashMap<CellLocation, GravityCell>, resolution: Float) {
        textures.white_pixel.bind()
        val maxMass = gravityMap.maxBy { (_, cell) -> cell.totalMass }!!.value.totalMass
        val sqrt1 = 0.707106781f
        gravityMap.forEach { (key, cell) ->
            val data = BasicShapes.polygon4.chunked(2)
                .flatMap {
                    listOf(
                        it[0], it[1], 0f,
                        1f, 1f, 1f, sqrt(cell.totalMass / maxMass) * .9f,
                        (it[0] / 2 - 0.5f), (it[1] / 2 - 0.5f)
                    )
                }.toFloatArray()
            renderer.drawShape(
                data,
                key.x * resolution,
                key.y * resolution,
                PI.toFloat() * .25f,
                resolution * sqrt1,
                resolution * sqrt1
            )
        }
    }

    fun drawPicture(texture: Texture, scale: Vec2 = Vec2(1f, 1f), offset: Vec2 = Vec2()) {
        texture.bind()

        val left = -texture.width / 2f
        val right = texture.width / 2f
        val top = texture.height / 2f
        val bottom = -texture.height / 2f

        val data = listOf(left, bottom, left, top, right, top, right, bottom).chunked(2)
            .flatMap {
                listOf(
                    it[0], it[1], 0f,
                    1f, 1f, 1f, 1f,
                    (it[0] / 2 - 0.5f) * scale.x + offset.x,
                    (it[1] / 2 - 0.5f) * scale.y + offset.y
                )
            }.toFloatArray()

        renderer.drawShape(data, 0f, 0f, 0f, 45f, 45f)
    }

}
