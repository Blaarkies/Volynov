@file:Suppress("UNCHECKED_CAST")

package utility

import display.draw.Model
import display.draw.TextureEnum
import display.graphic.CameraType
import display.graphic.vertex.Triangle
import display.graphic.vertex.Vertex
import exceptions.*
import org.joml.Vector2f
import org.joml.Vector3f
import utility.Common.getSafePath
import java.io.File

object WavefrontObject {

    private const val fileNameObject = "object.obj"
    private const val fileNameMaterial = "material.mtl"

    fun import(resourcePath: String): Model {
        val safePath = getSafePath("$resourcePath/$fileNameObject")
        val file = File(safePath)
        val a = parseContents(file.readLines())

        return Model(a, listOf(TextureEnum.metal), 1f, CameraType.UNIVERSE)
    }

    fun export(model: Model, path: String = "models") {
        val nameObject = "MyModel"
        val nameMaterial = "${nameObject}_material"
        val vertexIndices = model.triangles.flatMap { it.vertices }.distinct().withIndex().toList()

        val result = """
        # Blaarkies Volynov Object Exporter - OBJ File
        # https://github.com/Blaarkies/Volynov
        mtllib ${nameMaterial}.mtl
        o $nameObject
        ${vertexIndices.joinToString(System.lineSeparator()) { (_, it) -> 
            "v ${it.location.x} ${it.location.y} ${it.location.z}" }}
        ${vertexIndices.joinToString(System.lineSeparator()) { (_, it) ->
            "vt ${it.texture.x} ${it.texture.y}" }}
        ${model.triangles
                .map { "vn ${it.normal.x} ${it.normal.y} ${it.normal.z}" }
                .joinToString(System.lineSeparator())}
        usemtl $nameMaterial
        s off
        ${model.triangles.withIndex().joinToString(System.lineSeparator()) { (triangleIndex, triangle) ->
                "f " +
                triangle.vertices
                    .map { vertex -> vertexIndices.find { it.value == vertex }!!.index }
                    .joinToString(" ") { "${it + 1}/1/${triangleIndex + 1}" }
            }}
        """.trimIndent()

        val safePath = getSafePath(path)
        val file = File("$safePath/export_$nameObject.obj")
        file.writeText(result)
    }

    private fun parseContents(lines: List<String>): List<Triangle> {
        val vertexStart = lines.indexOfFirst { it.startsWith("v ") }
        val textureStart = lines.indexOfFirst { it.startsWith("vt ") }
        val normalStart = lines.indexOfFirst { it.startsWith("vn ") }
        val materialStart = lines.indexOfFirst { it.startsWith("usemtl ") }
        val faceStart = lines.indexOfFirst { it.startsWith("f ") }

        val verticesTextLines = lines.subList(vertexStart, textureStart)
        val textureTextLines = lines.subList(textureStart, normalStart)
        val normalsTextLines = lines.subList(normalStart, materialStart)
        val facesTextLines = lines.subList(faceStart, lines.size)

        val indexVertexMap = parseVectorsIntoHashMap<Vector3f>(verticesTextLines)
        val indexTextureMap = parseVectorsIntoHashMap<Vector2f>(textureTextLines)
        val indexNormalMap = parseVectorsIntoHashMap<Vector3f>(normalsTextLines)
        val indicesByFace = parseFacesIntoTriangles(facesTextLines)

        val triangles = indicesByFace.map {
            val vertices = it.map { (location, texture, normal) ->
                Vertex(
                    indexVertexMap[location] ?: throw VectorIndexNotFoundException(indexVertexMap, location),
                    indexTextureMap[texture] ?: throw VectorIndexNotFoundException(indexTextureMap, texture),
                    indexNormalMap[normal] ?: throw VectorIndexNotFoundException(indexNormalMap, normal),
                )
            }
            Triangle(vertices)
        }

        return triangles
    }

    private fun parseFacesIntoTriangles(dataLines: List<String>, dataSeparator: String = " ")
            : List<List<Triple<Int, Int, Int>>> {
        return dataLines.drop(1).withIndex()
            .map { (index, line) ->
                line.split(dataSeparator).drop(1)
                    .map { vertex ->
                        // Each vertex has 3 components: Location, Texture, Normal
                        val components = vertex.split("/")
                            .map {
                                Try { it.toInt() }
                                    .recoverWith { Failure<Int>(ParseFaceException("$it\n$line", index)) }
                                    .result
                            }
                        Triple(components[0], components[1], components[2])
                    }
            }
    }

    private fun <TypeVector> parseVectorsIntoHashMap(dataLines: List<String>, dataSeparator: String = " ")
            : Map<Int, TypeVector> {
        return dataLines.asSequence().withIndex()
            .map { (index, line) -> Pair(index + 1, line) }
            .map { (index, line) ->
                val coordinates = line.split(dataSeparator).drop(1)
                    .map {
                        try {
                            it.toFloat()
                        } catch (e: NumberFormatException) {
                            throw ParseVectorException(line, index, e)
                        }
                    }
                Pair(index, coordinates)
            }
            .map { (index, p) ->
                Pair(
                    index,
                    when (p.size) {
                        3 -> Vector3f(p[0], p[1], p[2])
                        2 -> Vector2f(p[0], p[1])
                        else -> throw ParseVectorException(p.toString(), index)
                    } as TypeVector
                )
            }
            .toMap()
    }

}
