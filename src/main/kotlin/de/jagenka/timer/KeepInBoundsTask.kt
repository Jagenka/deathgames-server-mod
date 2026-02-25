package de.jagenka.timer

import de.jagenka.config.Config
import de.jagenka.managers.PlayerManager

object KeepInBoundsTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val isGameMechanic: Boolean
        get() = false
    override val runEvery: Int
        get() = 1.ticks()

    override fun run()
    {
        PlayerManager.getOnlineParticipatingPlayers().forEach { player ->
            if (!Config.general.arenaBounds.contains(player.position()) && !PlayerManager.hasRecentlyRespawned(player.name.string))
            {
                // 1.21.3: damage now needs a server world
                player.hurtServer(
                    player.level(),
                    player.damageSources().outOfBorder(),
                    10f
                ) // changed from out of world to out of border
            }
        }
    }

    override fun reset()
    {

    }
}