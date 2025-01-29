package de.jagenka.timer

import de.jagenka.managers.MoneyManager
import de.jagenka.managers.PlayerManager
import de.jagenka.stats.StatManager

object AverageBalanceTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val isGameMechanic: Boolean
        get() = false
    override val runEvery: Int
        get() = 1

    override fun run()
    {
        PlayerManager.getParticipatingPlayers().forEach { playerName ->
            StatManager.updateAccountBalanceAverage(playerName, MoneyManager.getMoney(playerName))
        }
    }

    override fun reset()
    {
        // nothing to reset
    }
}