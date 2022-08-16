package de.jagenka.gameplay.rendering

import de.jagenka.Util
import de.jagenka.floor
import de.jagenka.managers.PlayerManager
import de.jagenka.rotateAroundVector
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object ParticleRenderer {

    private val model =
        PlyImporter.parsePlyFromFile("C:/Programming Projects/deathgames-server-mod/src/main/resources/models/Squirtle.ply")

    fun renderCube()
    {
        Util.ifServerLoaded { server ->
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

    fun generateLine(point1: Vec3d, point2: Vec3d, vertexSpacing: Double): List<Vec3d>
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

    fun drawParticlesFromVertexStructure(server: MinecraftServer, player: ServerPlayerEntity, particle: ParticleEffect, edges: VertexStructure)
    {
        edges.getSet().forEach { edge ->
            drawMultipleParticlesWorld(server, player, particle, generateLine(edge.point1, edge.point2, 0.1))
        }
    }

    fun drawParticlesFromVertexStructures(server: MinecraftServer, player: ServerPlayerEntity, particle: ParticleEffect, structures: Collection<VertexStructure>)
    {
        structures.forEach {
            drawParticlesFromVertexStructure(server, player, particle, it)
        }
    }

    fun getRandomNormalVector(): Vec3d
    {
        val phi = Random.nextDouble(0.0, 2 * PI)
        val cosTheta = Random.nextDouble(-1.0, 1.0)

        val theta = acos(cosTheta)
        val x = sin(theta) * cos(phi)
        val y = sin(theta) * sin(phi)

        // z is cosTheta
        return Vec3d(x, y, cosTheta)
    }

    fun drawParticlesFromVertexTreeElement(server: MinecraftServer, player: ServerPlayerEntity, particle: ParticleEffect, vertex: VertexTreeElement)
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

    fun drawMultipleParticlesWorld(server: MinecraftServer, player: ServerPlayerEntity, particle: ParticleEffect, vertices: Collection<Vec3d>)
    {
        vertices.forEach {vertex: Vec3d ->
            server.overworld.spawnParticles(player, particle, true, vertex.x, vertex.y, vertex.z, 1, 0.0, 0.0, 0.0, 0.0)
        }
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
        override fun equals(other: Any?): Boolean
        {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Edge

            if (point1 != other.point1) return false
            if (point2 != other.point2) return false

            return true
        }

        override fun hashCode(): Int
        {
            var result = point1.hashCode()
            result = 31 * result + point2.hashCode()
            return result
        }
    }

    class VertexStructure
    {
        private var edges: MutableSet<Edge> = mutableSetOf()

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

        fun getVertices(): List<Vec3d>
        {
            return edges.flatMap { listOf(it.point1, it.point2) }
        }

        fun scale(value: Double, origin: Vec3d = Vec3d.ZERO)
        {
            edges = edges.map {
                val newP1 = it.point1.subtract(origin).multiply(value).add(origin)
                val newP2 = it.point2.subtract(origin).multiply(value).add(origin)
                Edge(newP1, newP2)
            }.toMutableSet()
        }

        fun rotate(vector: Vec3d, degrees: Double, origin: Vec3d = Vec3d.ZERO)
        {
            edges = edges.map {
                val newP1 = it.point1.subtract(origin).rotateAroundVector(vector, degrees.toFloat()).add(origin)
                val newP2 = it.point2.subtract(origin).rotateAroundVector(vector, degrees.toFloat()).add(origin)
                Edge(newP1, newP2)
            }.toMutableSet()
        }

        fun translate(vector: Vec3d)
        {
            edges = edges.map {
                val newP1 = it.point1.add(vector)
                val newP2 = it.point2.add(vector)
                Edge(newP1, newP2)
            }.toMutableSet()
        }

        fun isEmpty(): Boolean
        {
            return edges.isEmpty()
        }

        fun clone(): VertexStructure {
            val newVS = VertexStructure()
            edges.map {
                newVS.add(it)
            }
            return newVS
        }
    }
}