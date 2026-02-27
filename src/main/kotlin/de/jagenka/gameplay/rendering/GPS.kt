package de.jagenka.gameplay.rendering

import de.jagenka.Util.ifServerLoaded
import de.jagenka.managers.BonusManager
import de.jagenka.managers.PlayerManager
import de.jagenka.rotateAroundVector
import de.jagenka.toRadians
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3

object GPS
{
    fun showArrowToNextBonusPlatform()
    {
        val origin = Vec3(0.0, 4.0, 0.0)
        ifServerLoaded { server: MinecraftServer ->
            PlayerManager.getOnlinePlayers().forEach { player: ServerPlayer ->
                BonusManager.selectedPlatforms.forEach platforms@{
                    var lookDirection = it.pos.toVec3d().subtract(player.position().add(origin))
                    if (lookDirection.length() < 10) return@platforms
                    lookDirection = lookDirection.normalize()
                    val lookDirectionXZImage =
                        Vec3(lookDirection.x, 0.0, lookDirection.z).yRot(90f.toRadians()).normalize()
                    val localYAxis = lookDirection.cross(lookDirectionXZImage).normalize()
                    val arrow = ParticleRenderer.VertexTreeElement(
                        origin.add(
                            player.forward.normalize().scale(2.0)
                        )
                    ) // move arrow forward, so it can be seen better
                    arrow
                        .makeChildByOffset(lookDirection.scale(-1.0))
                        .up()
                        .makeChildByOffset(lookDirection.scale(2.0))
                        .makeChildByOffset(lookDirection.rotateAroundVector(localYAxis, 135.0).scale(1.0))
                        .up()
                        .makeChildByOffset(lookDirection.rotateAroundVector(localYAxis, -135.0).scale(1.0))
                    ParticleRenderer.drawParticlesFromVertexTreeElement(server, player, ParticleTypes.ELECTRIC_SPARK, arrow)
                }
            }
        }
    }
}