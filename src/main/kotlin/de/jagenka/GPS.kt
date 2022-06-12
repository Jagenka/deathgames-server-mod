package de.jagenka

import de.jagenka.Util.ifServerLoaded
import de.jagenka.managers.BonusManager
import de.jagenka.managers.PlayerManager
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
                BonusManager.getSelectedPlatforms().forEach {
                    val arrow = VertexTreeElement(origin)
                    var lookDirection = it.coordinates.toVec3d().subtract(player.pos.add(origin))
                    if (lookDirection.length() < 10) return@forEach
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
                    drawParticlesFromVertices(server, player, ParticleTypes.WAX_OFF, arrow)
                }
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

    private fun drawParticlesFromVertices(server: MinecraftServer, player: ServerPlayerEntity, particle: ParticleEffect, vertex: VertexTreeElement)
    {
        if (vertex.children.isEmpty()) return
        for (child in vertex.children)
        {
            drawParticlesFromVertices(server, player, particle, child)
            drawMultipleParticles(server, player, particle, generateLine(vertex.position, child.position, 0.1))
        }
    }

    private fun drawMultipleParticles(server: MinecraftServer, player: ServerPlayerEntity, particle: ParticleEffect, vertices: Collection<Vec3d>)
    {
        val baseX = player.pos.x
        val baseY = player.pos.y
        val baseZ = player.pos.z
        vertices.forEach {vertex: Vec3d ->
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
            return parent?:this
        }
    }

    class VectorBase()
    {
        private val x: Vec3d = Vec3d(1.0, 0.0, 0.0)
        private val y: Vec3d = Vec3d(0.0, 1.0, 0.0)
        private val z: Vec3d = Vec3d(0.0, 0.0, 1.0)

    }
}