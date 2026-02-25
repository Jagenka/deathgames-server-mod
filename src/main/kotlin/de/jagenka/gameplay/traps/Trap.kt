package de.jagenka.gameplay.traps

import de.jagenka.BlockPos
import de.jagenka.Util.teleport
import de.jagenka.getDGCoordinates
import de.jagenka.managers.PlayerManager.getOnlinePlayersAround
import de.jagenka.stats.StatManager
import de.jagenka.stats.gib
import de.jagenka.timer.Timer
import de.jagenka.toCenter
import net.minecraft.core.Holder
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.effect.MobEffectInstance

class Trap(
    val position: BlockPos,

    val snares: Boolean,
    val effects: List<MobEffectInstance>,
    val triggerRange: Double,
    val setupTime: Int,
    val triggerVisibilityRange: Double,
    val visibilityRange: Double,
    val affectedRange: Double,
    val triggerDuration: Int,
)
{
    var doneSettingUp: Boolean = false
    var triggered: Boolean = false

    /**
     * each trap handles their own snaring, so ending snaring is easier (trap just gets deleted)
     */
    private val snaredPlayers = mutableSetOf<SnaredPlayer>()

    fun startSettingUp()
    {
        if (doneSettingUp || triggered) return // no need to set up, if already set up or triggered

        Timer.schedule(setupTime) { doneSettingUp = true }
    }

    fun trigger(affectedPlayers: Set<ServerPlayer>)
    {
        if (!doneSettingUp || triggered) return // cannot trigger, if not set up, or already triggered

        val (x, y, z) = position

        // show/play trigger effects
        getOnlinePlayersAround(position, triggerVisibilityRange).forEach { player ->
            // exploding particles
            player.level().sendParticles(
                player, ParticleTypes.LARGE_SMOKE, true, true, x.toCenter(), y.toDouble(), z.toCenter(), 500, .0, .0, .0, .5
            )
            // play sound
            player.connection.send(
                ClientboundSoundPacket(
                    Holder.direct(SoundEvents.IRON_GOLEM_HURT), // TODO: works? rewritten as per playsound command
                    SoundSource.PLAYERS,
                    x.toCenter(),
                    y.toDouble(),
                    z.toCenter(),
                    1f,
                    1f,
                    player.level().random.nextLong()
                )
            )
        }

        // apply potion effects
        affectedPlayers.forEach { player ->
            effects.forEach {
                player.addEffect(MobEffectInstance(it))
            }

            // increase stat
            StatManager.personalStats.gib(player.name.string).timesCaughtInTrap++
        }

        // snaring happens only after landing, so we cannot determine snare location here
        snaredPlayers.addAll(affectedPlayers.map { SnaredPlayer(it, null) })

        triggered = true
        Timer.schedule(triggerDuration) { TrapManager.removeTrap(this) }
    }

    fun handleSnaring()
    {
        if (!snares) return

        snaredPlayers.forEach {
            if (it.coordinates == null && it.player.onGround()) // snaring happens after landing
            {
                it.coordinates = it.player.getDGCoordinates()
            } else
            {
                it.player.teleport(it.coordinates)
            }
        }
    }

    /**
     * dead players no longer need to be snared, so they get removed here
     */
    fun onPlayerDeath(playerName: String)
    {
        snaredPlayers.removeAll { it.player.name.string == playerName }
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as Trap
        return this.position == other.position
    }

    override fun hashCode(): Int
    {
        return position.hashCode()
    }
}

