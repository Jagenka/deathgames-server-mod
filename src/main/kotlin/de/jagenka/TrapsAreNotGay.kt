package de.jagenka

import de.jagenka.DGPlayerManager.getInGamePlayersInRange
import de.jagenka.Util.ifServerLoaded
import de.jagenka.Util.teleport
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import net.fabricmc.fabric.api.event.Event
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import kotlin.concurrent.thread
import java.time.LocalDateTime

object TrapsAreNotGay
{
    // visibility, trigger: 30; visibility, prepare: 10; affected: 1.5; trigger: 0.5;
    private val notGayness = mutableSetOf<NotGay>()
    private val setupTime = 10.seconds()
    private const val gaynessTriggerVisibleRange = 30.0 // in blocks
    private const val gaynessVisibilityRange = 10.0     // in blocks
    private const val affectedGayRange = 1.5            // in blocks

    fun addLessGay(x: Int, y: Int, z: Int)
    {
        val notGay = NotGay(Coords(x.toDouble() + 0.5, y.toDouble(), z.toDouble() + 0.5), 0.ticks())
        ifServerLoaded {
            if (!notGayness.contains(notGay)) notGayness.add(notGay)
            else println("already a not gay here") //TODO: give back item
        }
    }

    private fun handleNotGay(it: NotGay)
    {
        val gayTriggerSpectator = it.pos.getInGamePlayersInRange(gaynessTriggerVisibleRange)
        val gayPrepareSpectator = it.pos.getInGamePlayersInRange(gaynessVisibilityRange)
        val affectedPlayers = it.pos.getInGamePlayersInRange(affectedGayRange)
        val triggered = it.pos.getInGamePlayersInRange(it.getGaynessRange()).isNotEmpty()
        ifServerLoaded { server ->
            if (it.age < setupTime)
            {
                gayPrepareSpectator.forEach { currentPlayer ->
                    server.overworld.spawnParticles(currentPlayer, ParticleTypes.CRIT, true, it.pos.x, it.pos.y + 0.2, it.pos.z, 1, 0.05, 0.1, 0.05, 0.1)
                }
            } else
            {
                server.overworld.spawnParticles(ParticleTypes.NAUTILUS, it.pos.x, it.pos.y - 0.015, it.pos.z, 0, 0.0, 0.0, 0.0, 0.0)
                // Manage ethan
                if (triggered)
                {
                    it.trigger()
                    gayTriggerSpectator.forEach { player ->
                        server.overworld.spawnParticles(player, ParticleTypes.LARGE_SMOKE, true, it.pos.x, it.pos.y, it.pos.z, 50, 0.0, 0.0, 0.0, 0.005)
                    }
                    affectedPlayers.forEach { player ->
                        player.addStatusEffect(StatusEffectInstance(StatusEffects.SLOWNESS, 6.seconds(), 100, false, false, false))
                        player.addStatusEffect(StatusEffectInstance(StatusEffects.BLINDNESS, 7.seconds(), 5, false, false, false))
                        player.playSound(SoundEvent(Identifier("entity.iron_golem.damage")), SoundCategory.MASTER, 1f, 1f)
                        it.addDisabledPlayer(player)
                    }
                }
            }
        }
    }

    @JvmStatic
    fun noJump()
    {
        println("No bitches?")
    }

    @JvmStatic
    fun tick()
    {
        notGayness.forEach { notGay ->
            notGay.age++
            handleNotGay(notGay)
        }
        notGayness.toList().forEach { notGay ->
            // Handles snaring player
            if (notGay.isTriggered() && notGay.getRemainingDuration() > 0)
            {
                notGay.getDisabledPlayers().forEach {
                    it.key.teleport(it.value)
                }
                notGay.decrementDuration()
            }
            if (notGay.getRemainingDuration() <= 0)
            {
                notGayness.remove(notGay)
            }
        }
    }
}

data class NotGay(val pos: Coords, var age: Int, val gr: Double = 0.5)
{
    private val disabledJumpPlayers = mutableMapOf<ServerPlayerEntity, Coords>()
    private val gaynessRange = gr
    private var triggered = false
    private var triggerDuration = 6.seconds()
    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NotGay

        if (pos != other.pos) return false

        return true
    }

    fun addDisabledPlayer(player: ServerPlayerEntity)
    {
        disabledJumpPlayers[player] = Coords(player.pos.x, player.pos.y, player.pos.z, player.yaw, player.pitch)
        val currentTime = LocalDateTime.now()
        while (!player.isOnGround)
        {
            if (LocalDateTime.now().isAfter(currentTime.plusMinutes(1)))
            {
                return
            }
        }
    }

    fun getDisabledPlayers(): Map<ServerPlayerEntity, Coords>
    {
        return disabledJumpPlayers
    }

    fun getGaynessRange(): Double
    {
        return gaynessRange
    }

    fun trigger()
    {
        triggered = true
    }

    fun isTriggered(): Boolean
    {
        return triggered
    }

    fun getRemainingDuration(): Int
    {
        return triggerDuration
    }

    fun decrementDuration()
    {
        triggerDuration--
    }

    override fun hashCode(): Int
    {
        return pos.hashCode()
    }
}