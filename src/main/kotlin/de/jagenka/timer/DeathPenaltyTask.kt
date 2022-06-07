package de.jagenka.timer

import de.jagenka.managers.DisplayManager.sendPrivateMessage
import de.jagenka.managers.KillManager
import de.jagenka.managers.PlayerManager

object DeathPenaltyTask : TimerTask
{
    private val deathPenaltyInterval = 30.seconds()

    private val deadForTicks = mutableMapOf<String, Int>().withDefault { 0 }

    override val onlyInGame: Boolean
        get() = true
    override val runEvery: Int
        get() = 1.ticks()

    override fun run()
    {
        PlayerManager.getPlayers().forEach { playerName ->
            if (PlayerManager.isCurrentlyDead(playerName))
            {
                deadForTicks[playerName] = deadForTicks.getValue(playerName) + 1

                if (deadForTicks.getValue(playerName) > deathPenaltyInterval)
                {
                    PlayerManager.getOnlinePlayer(playerName)?.let {
                        KillManager.removeOneLife(it)
                        it.sendPrivateMessage("You just lost a life due to not respawning.")
                        deadForTicks[playerName] = 0
                    }
                }
            } else
            {
                deadForTicks[playerName] = 0
            }
        }
    }

    override fun reset()
    {
        deadForTicks.clear()
    }
}