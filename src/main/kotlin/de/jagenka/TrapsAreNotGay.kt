package de.jagenka

import de.jagenka.DGPlayerManager.getInGamePlayersInRange
import de.jagenka.Util.ifServerLoaded
import de.jagenka.Util.teleport
import de.jagenka.timer.seconds
import de.jagenka.timer.ticks
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.ItemUsageContext
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Direction

object TrapsAreNotGay
{
    // visibility, trigger: 30; visibility, prepare: 10; affected: 1.5; trigger: 0.5;
    private val notGayness = mutableSetOf<NotGay>()
    private val setupTime = 10.seconds()
    private const val gaynessTriggerVisibleRange = 30.0 // in blocks
    private const val gaynessVisibilityRange = 10.0     // in blocks
    private const val affectedGayRange = 1.5            // in blocks

    @JvmStatic
    fun addLessGay(x: Int, y: Int, z: Int)
    {
        val notGay = NotGay(Coordinates(x.toDouble() + 0.5, y.toDouble(), z.toDouble() + 0.5), 0.ticks())
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
                if (triggered && !it.isTriggered())
                {
                    it.trigger()
                    gayTriggerSpectator.forEach { player ->
                        server.overworld.spawnParticles(player, ParticleTypes.LARGE_SMOKE, true, it.pos.x, it.pos.y, it.pos.z, 500, 0.0, 0.0, 0.0, 0.5)
                    }
                    affectedPlayers.forEach { player ->
                        player.playSound(SoundEvents.ENTITY_IRON_GOLEM_HURT, SoundCategory.PLAYERS, 1f, 1f)
                        it.addDisabledPlayer(player)
                    }
                }
            }
        }
    }

    fun becomeGay()
    {
        notGayness.clear()
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
                notGay.getDisabledPlayers().forEach { (player, coordinatesMaybe) ->
                    if (coordinatesMaybe.flag && player.isOnGround)
                    {
                        coordinatesMaybe.coordinates = Coordinates(player.pos.x, player.pos.y, player.pos.z, player.yaw, player.pitch)
                        coordinatesMaybe.flag = false
                    }
                    coordinatesMaybe.coordinates?.let {coordinates ->
                        player.teleport(coordinates)
                    }
                    player.addStatusEffect(StatusEffectInstance(StatusEffects.BLINDNESS, 2.seconds(), 100, false, false, false))
                }
                notGay.decrementDuration()
            }
            if (notGay.getRemainingDuration() <= 0)
            {
                notGayness.remove(notGay)
            }
        }
    }

    @JvmStatic
    fun handleTrapPlacement(ctx: ItemUsageContext): Boolean
    {
        if (ctx.stack.name.asString() == "Snare Trap")
        {
            if (ctx.side == Direction.UP)
            {
                addLessGay(ctx.blockPos.x, ctx.blockPos.y + 1, ctx.blockPos.z)
                ctx.player?.inventory?.selectedSlot?.let { ctx.player?.inventory?.removeStack(it, 1) }
            }
            return true
        }
        return false
    }
}

data class DisabledPlayerCoordinateFetch(var flag: Boolean, var coordinates: Coordinates? = null)

data class NotGay(val pos: Coordinates, var age: Int, val gr: Double = 0.5)
{
    private val disabledJumpPlayers = mutableMapOf<ServerPlayerEntity, DisabledPlayerCoordinateFetch>()
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
        disabledJumpPlayers[player] = DisabledPlayerCoordinateFetch(true)
    }

    fun getDisabledPlayers(): Map<ServerPlayerEntity, DisabledPlayerCoordinateFetch>
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