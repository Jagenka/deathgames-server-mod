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

object GPS
{
    fun makeArrowGoBrrr()
    {
        val origin = Vec3d(0.0, 4.0, 0.0)
        ifServerLoaded { server: MinecraftServer ->
            PlayerManager.getOnlinePlayers().forEach { player: ServerPlayerEntity ->
                BonusManager.selectedPlatforms.forEach platforms@{
                    val arrow = ParticleRenderer.VertexTreeElement(origin)
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
                    ParticleRenderer.drawParticlesFromVertexTreeElement(server, player, ParticleTypes.WAX_OFF, arrow)
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
        AStar(from.surface(), to.surface()).path.forEach { ParticleRenderer.drawParticleAtBlockPosForPlayer(player, ParticleTypes.NOTE, it) }
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
}