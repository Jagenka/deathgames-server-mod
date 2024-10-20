package de.jagenka.timer

import de.jagenka.gameplay.traps.TrapManager
import de.jagenka.managers.PlayerManager.getOnlineParticipatingPlayersAround
import de.jagenka.managers.PlayerManager.getOnlinePlayersAround
import de.jagenka.toCenter
import net.minecraft.particle.ParticleTypes

object TrapTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val isGameMechanic: Boolean
        get() = true

    override val runEvery: Int
        get() = 1.ticks()

    override fun run()
    {
        TrapManager.traps.forEach { trap ->
            val (x, y, z) = trap.position
            val playersInGeneralVisibilityRange = getOnlinePlayersAround(trap.position, trap.visibilityRange)

            if (!trap.doneSettingUp) // setup animation
            {
                playersInGeneralVisibilityRange.forEach { player ->
                    player.serverWorld.spawnParticles(
                        player, ParticleTypes.CRIT, true, x.toCenter(), y + 0.2, z.toCenter(), 1, 0.05, 0.1, 0.05, 0.1
                    )
                }
            }

            if (trap.doneSettingUp && !trap.triggered) // not yet triggered trap handling
            {
                // play idle animation
                playersInGeneralVisibilityRange.forEach { player ->
                    player.serverWorld.spawnParticles(
                        ParticleTypes.NAUTILUS, x.toCenter(), y - 0.015, z.toCenter(), 0, 0.0, 0.0, 0.0, 0.0
                    )
                }

                // check if trap should be triggered
                val playersInTriggerRange = getOnlineParticipatingPlayersAround(trap.position, trap.triggerRange)
                if (playersInTriggerRange.isNotEmpty())
                {
                    trap.trigger(affectedPlayers = getOnlineParticipatingPlayersAround(trap.position, trap.affectedRange).toSet())
                }
            }

            // triggered traps may want to snare
            if (trap.doneSettingUp && trap.triggered)
            {
                trap.handleSnaring()
            }
        }
    }

    override fun reset()
    {
        // nothing to do...
    }
}