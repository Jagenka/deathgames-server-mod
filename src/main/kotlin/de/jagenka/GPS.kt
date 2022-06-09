package de.jagenka

import de.jagenka.Util.ifServerLoaded
import de.jagenka.managers.PlayerManager
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d

object GPS
{
    private val vertexTree = VertexTree()

    fun makeArrowGoBrrr()
    {
        val origin = Vec3d(0.0, 4.0, 0.0)
        ifServerLoaded { server: MinecraftServer ->
            PlayerManager.getOnlinePlayers().forEach { player: ServerPlayerEntity ->
                drawMultipleParticles(server, player, ParticleTypes.WAX_OFF, generateLine(origin, player.rotationVector, 4.0, 0.1))
            }
        }
    }

    private fun generateLine(origin: Vec3d, vector: Vec3d, magnitude: Double, vertexSpacing: Double): List<Vec3d>
    {
        val vertices: MutableList<Vec3d> = mutableListOf()
        vertices.add(Vec3d(origin.x, origin.y, origin.z))
        var iterationVector = origin
        val normalizedVector = vector.normalize()
        var steps = (magnitude / vertexSpacing).floor() + 1
        val stepLength = magnitude / steps
        while (steps >= 1)
        {
            iterationVector = iterationVector.add(normalizedVector.multiply(stepLength))
            vertices.add(iterationVector)
            steps--
        }
        return vertices
    }

    private fun drawMultipleParticles(server: MinecraftServer, player: ServerPlayerEntity, particle: ParticleEffect, vertices: Collection<Vec3d>)
    {
        val baseX = player.pos.x
        val baseY = player.pos.y
        val baseZ = player.pos.z
        val playerViewDirection = player.movementDirection
        vertices.forEach {vertex: Vec3d ->
            server.overworld.spawnParticles(player, particle, true, baseX + vertex.x, baseY + vertex.y, baseZ + vertex.z, 1, 0.0, 0.0, 0.0, 0.0)
        }
    }

    private fun packVertices(vararg vertex: Vec3d): List<Vec3d>
    {
        return vertex.toList()
    }

    class VertexTreeElement(val position: Vec3d, var parent: VertexTreeElement? = null, vararg child: VertexTreeElement)
    {
        private val children: MutableList<VertexTreeElement> = child.toMutableList()

        fun makeChildren(vararg childPosition: Vec3d): Collection<VertexTreeElement>
        {
            val childrenToAdd = mutableListOf<VertexTreeElement>()
            childPosition.forEach {
                childrenToAdd.add(VertexTreeElement(it, this))
            }
            children.addAll(childrenToAdd)
            return childrenToAdd
        }

        fun remove(): VertexTreeElement
        {
            parent?.removeChild(this)
            return this
        }

        private fun removeChild(element: VertexTreeElement)
        {
            children.remove(element)
        }
    }

    class VertexTree()
    {
        var elements = listOf<VertexTreeElement>()

        fun addOrigin(position: Vec3d)
        {
            val returnList = elements.toMutableList()
            returnList.add(VertexTreeElement(position))
            elements = returnList.toList()
        }

        fun addOnElement(origin: VertexTreeElement, vararg destination: Vec3d)
        {
            val returnList = elements.toMutableList()
            returnList.addAll(origin.makeChildren(*destination))
            elements = returnList.toList()
        }

        fun removeElement(element: VertexTreeElement)
        {
            val returnList = elements.toMutableList()
            returnList.remove(element.remove())
            elements = returnList.toList()
        }
    }
}