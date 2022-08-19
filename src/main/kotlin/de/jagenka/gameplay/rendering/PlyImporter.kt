package de.jagenka.gameplay.rendering

import net.minecraft.util.math.Vec3d
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

object PlyImporter
{
    private var reading: Boolean = false
    private var edges: ParticleRenderer.VertexStructure = ParticleRenderer.VertexStructure()
    private val vertexList: MutableList<ParticleRenderer.VertexTreeElement> = mutableListOf()
    private val faceList: MutableList<MutableList<Int>> = mutableListOf()

    fun parsePlyFromStream(stream: InputStream): ParticleRenderer.VertexStructure
    {
        reading = false
        edges = ParticleRenderer.VertexStructure()
        vertexList.clear()
        faceList.clear()

        InputStreamReader(stream, Charsets.UTF_8).use { inputReader ->
            BufferedReader(inputReader).use { bufferedReader ->
                val lines = bufferedReader.lines()

                var vertex_count = 0
                var face_count = 0
                lines.forEach forEachLine@{ line ->
                    if (line.startsWith("element vertex"))
                    {
                        vertex_count = line.removePrefix("element vertex ").toInt()
                    }
                    if (line.startsWith("element face"))
                    {
                        face_count = line.removePrefix("element face ").toInt()
                    }
                    if (line == "end_header")
                    {
                        reading = true
                        return@forEachLine
                    }
                    if (!reading) return@forEachLine

                    val elements = line.split(" ")
                    if (vertex_count > 0)
                    {
                        vertexList.add(
                            ParticleRenderer.VertexTreeElement(
                                Vec3d(
                                    elements[0].toDouble(),
                                    elements[1].toDouble(),
                                    elements[2].toDouble()
                                )
                            )
                        )
                        vertex_count--
                    }
                    else if (face_count > 0)
                    {
                        faceList.add(elements.drop(1).map { e -> e.toInt() } as MutableList<Int>)
                        face_count--
                    }
                }

                // Process lists
                for (polygon in faceList)
                {
                    polygon.forEachIndexed { index, vertexIndex ->
                        val derEineVertex = vertexList[vertexIndex].position
                        val derAndereVertex = vertexList[polygon[(index + 1).mod(polygon.size)]].position
                        edges.add(ParticleRenderer.Edge(derEineVertex, derAndereVertex))
                    }
                }
                return edges
            }
        }
    }
}