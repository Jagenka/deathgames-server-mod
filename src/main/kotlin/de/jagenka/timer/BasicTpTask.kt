package de.jagenka.timer

import de.jagenka.DeathGames
import de.jagenka.Util.teleport
import de.jagenka.config.Config
import de.jagenka.managers.PlayerManager
import de.jagenka.toDGCoordinates

object BasicTpTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = false
    override val runEvery: Int
        get() = 1.ticks()

    override fun run()
    {
        if (!DeathGames.running)
        {
            PlayerManager.getOnlinePlayers().forEach {
                if (!it.interactionManager.isSurvivalLike) return@forEach
                if (Config.arenaBounds.contains(it.pos.toDGCoordinates())) it.teleport(Config.lobbySpawn)
            }
        }
    }

    override fun reset()
    {

    }
}