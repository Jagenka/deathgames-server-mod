package de.jagenka.timer

import de.jagenka.config.Config.moneyInterval
import de.jagenka.config.Config.moneyPerInterval
import de.jagenka.managers.BonusManager
import de.jagenka.managers.DisplayManager
import de.jagenka.managers.MoneyManager.addMoney
import de.jagenka.managers.PlayerManager

object MoneyTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val runEvery: Int
        get() = 1.ticks()

    private var moneyTimer = 0

    override fun run()
    {
        if (moneyTimer >= moneyInterval)
        {
            PlayerManager.getPlayers().forEach { addMoney(it, moneyPerInterval) }
            moneyTimer -= moneyInterval
        }
        PlayerManager.getPlayers().forEach { playerName ->
            if (!BonusManager.isOnActivePlatform(playerName))
            {
                DisplayManager.setExpProgress(playerName, moneyTimer.toFloat() / moneyInterval.toFloat())
            } // else -> BonusMoneyTask
        }

        moneyTimer++
    }

    override fun reset()
    {
    }
}