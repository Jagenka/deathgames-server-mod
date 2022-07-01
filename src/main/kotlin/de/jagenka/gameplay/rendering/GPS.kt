package de.jagenka.gameplay.rendering

import de.jagenka.Util.ifServerLoaded
import de.jagenka.floor
import de.jagenka.managers.BonusManager
import de.jagenka.managers.PlayerManager
import de.jagenka.rotateAroundVector
import de.jagenka.toRadians
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d

object GPS
{
    fun makeArrowGoBrrr()
    {
        val origin = Vec3d(0.0, 4.0, 0.0)
        ifServerLoaded { server: MinecraftServer ->
            PlayerManager.getOnlinePlayers().forEach { player: ServerPlayerEntity ->
                BonusManager.selectedPlatforms.forEach platforms@{
                    val arrow = VertexTreeElement(origin)
                    var lookDirection = it.pos.toVec3d().subtract(player.pos.add(origin))
                    if (lookDirection.length() < 10) return@platforms
                    lookDirection = lookDirection.normalize()
                    val lookDirectionXZImage = Vec3d(lookDirection.x, 0.0, lookDirection.z).rotateY(90f.toRadians()).normalize()
                    val localYAxis = lookDirection.crossProduct(lookDirectionXZImage).normalize()
                    arrow
                        .makeChildByOffset(lookDirection.multiply(-1.0))
                        .up()
                        .makeChildByOffset(lookDirection.multiply(4.0))
                        .makeChildByOffset(lookDirection.rotateAroundVector(localYAxis, 135f).multiply(1.0))
                        .up()
                        .makeChildByOffset(lookDirection.rotateAroundVector(localYAxis, -135f).multiply(1.0))
                    drawParticlesFromVertexTreeElement(server, player, ParticleTypes.WAX_OFF, arrow)
                }
            }
        }
    }

    private val model =
        PlyImporter.parsePlyFromFile("C:/Programming Projects/deathgames-server-mod/src/main/resources/models/Squirtle.ply")

    fun renderCube()
    {
        ifServerLoaded { server ->
            PlayerManager.getOnlinePlayers().forEach { player ->
                val lookDirection = player.rotationVector.normalize()
                val offset = Vec3d(lookDirection.x, 0.0, lookDirection.z).multiply(3.0)
                val finalStructure = VertexStructure()
                if (model.isEmpty()) return@ifServerLoaded
                model.getSet().forEach { edge ->
                    finalStructure.add(Edge(edge.point1.add(offset), edge.point2.add(offset)))
                }
                drawParticlesFromVertexStructure(server, player, ParticleTypes.WAX_OFF, finalStructure)
            }
        }
    }

    private fun generateLine(point1: Vec3d, point2: Vec3d, vertexSpacing: Double): List<Vec3d>
    {
        val vertices: MutableList<Vec3d> = mutableListOf()
        val vector: Vec3d = point2.subtract(point1)
        val direction: Vec3d = vector.normalize()
        val magnitude: Double = vector.length()
        var iterationVector: Vec3d = point1
        vertices.add(iterationVector)
        var steps = (magnitude / vertexSpacing).floor() + 1
        val stepLength = magnitude / steps
        while (steps >= 1)
        {
            iterationVector = iterationVector.add(direction.multiply(stepLength))
            vertices.add(iterationVector)
            steps--
        }
        return vertices
    }

    private fun drawParticlesFromVertexStructure(server: MinecraftServer, player: ServerPlayerEntity, particle: ParticleEffect, edges: VertexStructure)
    {
        edges.getSet().forEach { edge ->
            drawMultipleParticles(server, player, particle, generateLine(edge.point1, edge.point2, 0.1))
        }
    }

    private fun drawParticlesFromVertexTreeElement(server: MinecraftServer, player: ServerPlayerEntity, particle: ParticleEffect, vertex: VertexTreeElement)
    {
        if (vertex.children.isEmpty()) return
        for (child in vertex.children)
        {
            drawParticlesFromVertexTreeElement(server, player, particle, child)
            drawMultipleParticles(server, player, particle, generateLine(vertex.position, child.position, 0.1))
        }
    }

    private fun drawMultipleParticles(server: MinecraftServer, player: ServerPlayerEntity, particle: ParticleEffect, vertices: Collection<Vec3d>)
    {
        val baseX = player.pos.x
        val baseY = player.pos.y
        val baseZ = player.pos.z
        vertices.forEach { vertex: Vec3d ->
            server.overworld.spawnParticles(player, particle, true, baseX + vertex.x, baseY + vertex.y, baseZ + vertex.z, 1, 0.0, 0.0, 0.0, 0.0)
        }
    }

    private fun packVertices(vararg vertex: Vec3d): List<Vec3d>
    {
        return vertex.toList()
    }

    class VertexTreeElement(val position: Vec3d, var parent: VertexTreeElement? = null, val children: MutableList<VertexTreeElement> = mutableListOf())
    {
        fun makeChild(child: VertexTreeElement): VertexTreeElement
        {
            child.parent = this
            children.add(child)
            return children.last()
        }

        fun makeChild(child: Vec3d): VertexTreeElement
        {
            return makeChild(VertexTreeElement(child))
        }

        fun makeChildren(vararg child: VertexTreeElement): List<VertexTreeElement>
        {
            val list = mutableListOf<VertexTreeElement>()
            child.forEach {
                makeChild(it)
                list.add(it)
            }
            return list
        }

        fun makeChildren(vararg childPos: Vec3d): List<VertexTreeElement>
        {
            return makeChildren(*childPos.toList().map { VertexTreeElement(it) }.toTypedArray())
        }

        fun makeChildByOffset(offset: Vec3d): VertexTreeElement
        {
            return makeChild(this.position.add(offset))
        }

        fun order66(vararg child: VertexTreeElement)
        {
            child.forEach {
                children.remove(it)
            }
        }

        fun up(): VertexTreeElement
        {
            return parent ?: this
        }
    }

    data class Edge(val point1: Vec3d, val point2: Vec3d)
    {
        fun equals(other: Edge): Boolean
        {
            return (point1 == other.point1) && (point2 == other.point2) || (point1 == other.point2) && (point2 == other.point1)
        }
    }

    class VertexStructure
    {
        private val edges: MutableSet<Edge> = mutableSetOf()

        fun add(edge: Edge): Boolean
        {
            return edges.add(edge)
        }

        fun remove(edge: Edge): Boolean
        {
            return edges.remove(edge)
        }

        fun getSet(): MutableSet<Edge>
        {
            return edges
        }

        fun isEmpty(): Boolean
        {
            return edges.isEmpty()
        }
    }
}