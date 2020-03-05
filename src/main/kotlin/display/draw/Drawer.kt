package display.draw

import display.graphic.BasicShapes
import display.graphic.Color
import display.graphic.Renderer
import display.graphic.Texture
import engine.freeBody.FreeBody
import engine.freeBody.Planet
import engine.freeBody.Vehicle
import engine.physics.CellLocation
import engine.physics.GravityCell
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.collision.shapes.PolygonShape
import java.util.HashMap
import kotlin.math.PI
import kotlin.math.sqrt

class Drawer(private val renderer: Renderer) {

    private lateinit var marble_earth: Texture
    private lateinit var metal: Texture
    private lateinit var pavement: Texture
    private lateinit var white_pixel: Texture

    fun init() {
        marble_earth = Texture.loadTexture("src\\main\\resources\\textures\\marble_earth.png")
        metal = Texture.loadTexture("src\\main\\resources\\textures\\metal.png")
        pavement = Texture.loadTexture("src\\main\\resources\\textures\\pavement.png")
        white_pixel = Texture.loadTexture("src\\main\\resources\\textures\\white_pixel.png")
    }

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

        white_pixel.bind()
//        renderer.drawStrip(data)

        renderer.drawText(freeBody.id, x, y, Color.WHITE)
    }

    fun drawTrail(freeBody: FreeBody) {
        val linePoints = freeBody.motion.trailers
            .chunked(2)
            .chunked(2)
            .filter { it.size > 1 }
            .flatMap {
                val (a, b) = it
                listOf(a[0], a[1], b[0], b[1])
            }
        val data = getLineFromPoints(linePoints, Color(0.4f, 0.7f, 1f, 0.5f), Color.TRANSPARENT, 2f, 0f)

        white_pixel.bind()
        renderer.drawStrip(data)
    }

    fun getLineFromPoints(
        points: List<Float>,
        startColor: Color = Color.WHITE,
        endColor: Color = startColor,
        startWidth: Float = 1f,
        endWidth: Float = startWidth
    ): FloatArray {
        val triangleStripPoints = BasicShapes.getTriangleStripPoints(points, startWidth, endWidth)
        val coloredData = getColoredData(triangleStripPoints, startColor, endColor)
        return coloredData.toFloatArray()
    }

    fun drawFreeBody(freeBody: FreeBody) {
        when {
            freeBody is Vehicle -> metal.bind()
            freeBody is Planet && freeBody.radius <= 30.0 -> pavement.bind()
            freeBody is Planet -> marble_earth.bind()
            else -> white_pixel.bind()
        }

        val textureScale = when {
            freeBody.shapeBox is CircleShape && freeBody.radius <= 30f -> 1.2f
            freeBody.shapeBox is CircleShape -> 1f
            freeBody.shapeBox is PolygonShape -> .02f
            else -> 1f
        }

        val data3 = when (freeBody.shapeBox) {
            is CircleShape -> BasicShapes.polygon30.chunked(2)
            is PolygonShape -> (freeBody.shapeBox as PolygonShape).vertices
                .flatMap { listOf(it.x, it.y) }.chunked(2)
            else -> listOf()
        }.flatMap {
            listOf(
                it[0], it[1], 0f,
                1f, 1f, 1f, 1f,
                (it[0] / 2 - 0.5f) * textureScale, (it[1] / 2 - 0.5f) * textureScale
            )
        }.toFloatArray()

        val scale = when {
            freeBody.shapeBox is CircleShape -> freeBody.shapeBox.radius
            freeBody.shapeBox is PolygonShape -> 1f
            else -> 1f
        }

        renderer.drawShape(
            data3,
            freeBody.worldBody.position.x,
            freeBody.worldBody.position.y,
            freeBody.worldBody.angle,
            scale,
            scale
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
        white_pixel.bind()
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

}
