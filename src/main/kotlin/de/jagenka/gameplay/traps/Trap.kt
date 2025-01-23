package de.jagenka.gameplay.traps

import de.jagenka.BlockPos
import de.jagenka.Util.teleport
import de.jagenka.getDGCoordinates
import de.jagenka.managers.PlayerManager.getOnlinePlayersAround
import de.jagenka.stats.StatManager
import de.jagenka.stats.gib
import de.jagenka.timer.Timer
import de.jagenka.toCenter
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.particle.ParticleTypes
import net.minecraft.registry.Registries
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents

class Trap(
    val position: BlockPos,

    val snares: Boolean,
    val effects: List<StatusEffectInstance>,
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

    fun trigger(affectedPlayers: Set<ServerPlayerEntity>)
    {
        if (!doneSettingUp || triggered) return // cannot trigger, if not set up, or already triggered

        val (x, y, z) = position

        // show/play trigger effects
        getOnlinePlayersAround(position, triggerVisibilityRange).forEach { player ->
            // exploding particles
            player.serverWorld.spawnParticles(
                player, ParticleTypes.LARGE_SMOKE, true, true, x.toCenter(), y.toDouble(), z.toCenter(), 500, .0, .0, .0, .5
            )
            // play sound
            player.networkHandler.sendPacket(
                PlaySoundS2CPacket(
                    Registries.SOUND_EVENT.getEntry(SoundEvents.ENTITY_IRON_GOLEM_HURT),
                    SoundCategory.PLAYERS,
                    x.toCenter(),
                    y.toDouble(),
                    z.toCenter(),
                    1f,
                    1f,
                    player.world.random.nextLong()
                )
            )
        }

        // apply potion effects
        affectedPlayers.forEach { player ->
            effects.forEach {
                player.addStatusEffect(StatusEffectInstance(it))
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
            if (it.coordinates == null && it.player.isOnGround) // snaring happens after landing
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

