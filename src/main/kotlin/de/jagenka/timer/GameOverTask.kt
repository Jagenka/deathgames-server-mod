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
        if (DGPlayerManager.getOnlineInGameTeams().size <= 1)
        {
            Util.sendChatMessage("GAME OVER") //TODO: change this
            Timer.pause() //TODO: weg?
            DeathGames.running = false
            DGPlayerManager.getOnlinePlayers().forEach { it.changeGameMode(GameMode.SPECTATOR) }
            //TODO: timer, back to lobby, etc
        }
    }

    override fun reset()
    {
    }
}