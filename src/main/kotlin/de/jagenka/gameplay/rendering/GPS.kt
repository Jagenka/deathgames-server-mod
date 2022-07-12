package de.jagenka.gameplay.rendering

import de.jagenka.BlockPos
import de.jagenka.BlockPos.Companion.toDGBlockPos
import de.jagenka.Util
import de.jagenka.Util.ifServerLoaded
import de.jagenka.managers.BonusManager
import de.jagenka.managers.PlayerManager
import de.jagenka.rotateAroundVector
import de.jagenka.toRadians
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d
import kotlin.math.max
import kotlin.math.sqrt

object GPS
{
    fun showArrowToNextBonusPlatform()
    {
        val origin = Vec3d(0.0, 4.0, 0.0)
        ifServerLoaded { server: MinecraftServer ->
            PlayerManager.getOnlinePlayers().forEach { player: ServerPlayerEntity ->
                BonusManager.selectedPlatforms.forEach platforms@{
                    var lookDirection = it.pos.toVec3d().subtract(player.pos.add(origin))
                    if (lookDirection.length() < 10) return@platforms
                    lookDirection = lookDirection.normalize()
                    val lookDirectionXZImage = Vec3d(lookDirection.x, 0.0, lookDirection.z).rotateY(90f.toRadians()).normalize()
                    val localYAxis = lookDirection.crossProduct(lookDirectionXZImage).normalize()
                    val arrow = ParticleRenderer.VertexTreeElement(origin.add(player.rotationVector.normalize().multiply(2.0))) // move arrow forward, so it can be seen better
                    arrow
                        .makeChildByOffset(lookDirection.multiply(-1.0))
                        .up()
                        .makeChildByOffset(lookDirection.multiply(2.0))
                        .makeChildByOffset(lookDirection.rotateAroundVector(localYAxis.toVector3f(), 135f).multiply(1.0))
                        .up()
                        .makeChildByOffset(lookDirection.rotateAroundVector(localYAxis.toVector3f(), -135f).multiply(1.0))
                    ParticleRenderer.drawParticlesFromVertexTreeElement(server, player, ParticleTypes.ELECTRIC_SPARK, arrow)
                }
            }
        }
    }

    fun drawPathToPlatformForAllPlayers()
    {
        BonusManager.selectedPlatforms.forEach { platform ->
            PlayerManager.getOnlinePlayers().forEach forEachPlayer@{ player ->
                if(platform.pos distanceTo player.pos < 10) return@forEachPlayer
                drawPathTo(player, player.blockPos.toDGBlockPos(), platform.pos)
            }
        }
    }

    fun drawPathTo(player: ServerPlayerEntity, from: BlockPos, to: BlockPos)
    {
        aStar(from.surface(), to.surface()).forEach { ParticleRenderer.drawParticleAtBlockPosForPlayer(player, ParticleTypes.NOTE, it) }
    }

    fun BlockPos.surface() = getGroundSurfaceBlockPos(this)
    fun getGroundSurfaceBlockPos(blockPos: BlockPos): BlockPos
    {
        var result = blockPos
        while (Util.canPlayerStandIn(result))
        {
            result = result.relative(y = -1)
        }
        while (!Util.canPlayerStandIn(result))
        {
            result = result.relative(y = +1)
        }
        return result
    }

    fun h(pos: BlockPos, goal: BlockPos): Double
    {
        return pos.distanceTo(goal.toVec3d())
    }

    fun d(pos: BlockPos, neighbor: BlockPos): Double
    {
        val (dx, dy, dz) = neighbor - pos
        return if (dy == 0)
        {
            sqrt((dx * dx + dz * dz).toDouble())
        } else if (dy > 0)
        {
            pos.manhattanDistanceTo(neighbor).toDouble()
        } else
        {
            (pos.manhattanDistanceTo(neighbor) + max(0, dy - 2)).toDouble()
        }
    }

    fun aStar(start: BlockPos, goal: BlockPos): List<BlockPos>
    {
        val openSet = mutableSetOf(start)
        val cameFrom = mutableMapOf<BlockPos, BlockPos>()
        val gScore = mutableMapOf<BlockPos, Double>().withDefault { Double.MAX_VALUE }
        gScore[start] = 0.0
        val fScore = mutableMapOf<BlockPos, Double>().withDefault { Double.MAX_VALUE }
        fScore[start] = h(start, goal)

        while (openSet.isNotEmpty())
        {
            val current = openSet.minByOrNull { h(it, goal) }!!
            if (current == goal) return reconstructPath(cameFrom, goal)

            openSet.remove(current)
            current.getPossibleWalkDestinations().forEach {
                val tentativeGScore = gScore.getValue(current) + d(current, it)
                if (tentativeGScore < gScore.getValue(it))
                {
                    cameFrom[it] = current
                    gScore[it] = tentativeGScore
                    fScore[it] = tentativeGScore + h(it, goal)
                    if (it !in openSet) openSet.add(it)
                }
            }
        }

        return emptyList()
    }

    fun reconstructPath(cameFrom: Map<BlockPos, BlockPos>, goal: BlockPos): List<BlockPos>
    {
        val path = mutableListOf<BlockPos>()
        var current: BlockPos? = goal
        while (current != null)
        {
            path.add(0, current)
            current = cameFrom[current]
        }
        return path
    }
}