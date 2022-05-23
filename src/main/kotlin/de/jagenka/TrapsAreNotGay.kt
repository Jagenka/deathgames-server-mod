package de.jagenka

import de.jagenka.DGPlayerManager.getInGamePlayersInRange
import de.jagenka.Util.ifServerLoaded
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d

object TrapsAreNotGay
{
    private val notGayness = mutableSetOf<NotGay>()
    private val setupTime = 10.seconds()
    private const val gaynessVisibilityRange = 10   // ten blocks visibility

    fun addLessGay(x: Int, y: Int, z: Int)
    {
        val notGay = NotGay(Coordinates(x.toDouble() + 0.5, y.toDouble(), z.toDouble() + 0.5), 0.ticks())
        ifServerLoaded {
            if (!notGayness.contains(notGay)) notGayness.add(notGay)
            else println("already a not gay here") //TODO: give back item
        }
    }

    private fun handleNotGay(player: ServerPlayerEntity)
    {
        notGayness.toList().forEach {
            val playerDistance = Coordinates(player.x, player.y, player.z) distanceTo it.pos
            ifServerLoaded { server ->
                // Manage particles
                if (playerDistance <= gaynessVisibilityRange)
                {
                    if (it.age < setupTime)
                    {
                        server.overworld.spawnParticles(player, ParticleTypes.CRIT, true, it.pos.x, it.pos.y + 0.2, it.pos.z, 1, 0.05, 0.1, 0.05, 0.1)
                    } else
                    {
                        server.overworld.spawnParticles(player, ParticleTypes.NAUTILUS, true, it.pos.x, it.pos.y - 0.015, it.pos.z, 0, 0.0, 0.0, 0.0, 0.0)
                        // Manage ethan
                        if (playerDistance <= 1.5)
                        {
                            player.addStatusEffect(StatusEffectInstance(StatusEffects.SLOWNESS, 6, 100, false, false, false))
                            player.addStatusEffect(StatusEffectInstance(StatusEffects.BLINDNESS, 7, 5, false, false, false))
                            /*player.playSound(
                                SoundEvent(Identifier("entity.iron_golem.damage")),
                                SoundCategory.MASTER,
                                1f,
                                1f
                            )*/
                            player.networkHandler.sendPacket(
                                PlaySoundIdS2CPacket(
                                    Identifier("entity.iron_golem.damage"),
                                    SoundCategory.MASTER,
                                    Vec3d(it.pos.x, it.pos.y, it.pos.z).normalize().multiply(2.0),
                                    1f,
                                    1f
                                )
                            )
                            notGayness.remove(it)
                        }
                    }
                }
            }
        }
    }

    @JvmStatic
    fun tick()
    {
        notGayness.forEach { notGay ->
            notGay.age++
            val relevantPlayers = notGay.pos.getInGamePlayersInRange(1.5)
            relevantPlayers.forEach {
                if (notGay.triggersNotGay(it))
                {
                    TODO("Implement functionality based on distance to object")
                }
                handleNotGay(it)
            }
        }
    }
}

data class NotGay(val pos: Coordinates, var age: Int)
{
    private val gaynessRange = 0.5  // half a block radius
    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NotGay

        if (pos != other.pos) return false

        return true
    }

    fun triggersNotGay(player: ServerPlayerEntity): Boolean
    {
        return (Coordinates(player.pos.x, player.pos.y, player.pos.z) distanceTo this.pos) <= gaynessRange
    }

    override fun hashCode(): Int
    {
        return pos.hashCode()
    }
}