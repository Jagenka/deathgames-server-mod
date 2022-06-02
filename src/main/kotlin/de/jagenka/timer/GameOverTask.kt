package de.jagenka.timer

import de.jagenka.DeathGames
import de.jagenka.Util
import de.jagenka.managers.PlayerManager
import net.minecraft.world.GameMode

object GameOverTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val runEvery: Int
        get() = 1.ticks()

    override fun run()
    {
        if (PlayerManager.getOnlineInGameTeams().size <= 1)
        {
            DeathGames.stopGame()
        }
    }

    override fun reset()
    {
    }
}