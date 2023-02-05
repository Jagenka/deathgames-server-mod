package de.jagenka.gameplay.rendering

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
                        .makeChildByOffset(lookDirection.rotateAroundVector(localYAxis.toVector3f(), 135f).multiply(1.0))
                        .up()
                        .makeChildByOffset(lookDirection.rotateAroundVector(localYAxis.toVector3f(), -135f).multiply(1.0))
                    ParticleRenderer.drawParticlesFromVertexTreeElement(server, player, ParticleTypes.WAX_OFF, arrow)
                }
            }
        }
    }
}