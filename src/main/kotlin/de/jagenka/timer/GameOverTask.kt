package de.jagenka.timer

import de.jagenka.DGPlayerManager
import de.jagenka.DeathGames
import de.jagenka.Util
import net.minecraft.world.GameMode

object GameOverTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val runEvery: Int
        get() = 1.ticks()

    override fun run()
    {
        if (DGPlayerManager.getInGameTeams().size <= 1)
        {
            Util.sendChatMessage("GAME OVER")
            Timer.pause()
            DeathGames.running = false
            DGPlayerManager.getPlayers().forEach { it.changeGameMode(GameMode.SPECTATOR) }
            //TODO: timer, back to lobby, etc
        }
    }

    override fun reset()
    {
    }
}