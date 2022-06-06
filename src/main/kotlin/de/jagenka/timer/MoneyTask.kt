package de.jagenka.timer

import de.jagenka.config.Config.moneyInterval
import de.jagenka.config.Config.moneyPerInterval
import de.jagenka.managers.MoneyManager.addMoney
import de.jagenka.managers.PlayerManager

object MoneyTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val runEvery: Int
        get() = moneyInterval

    override fun run()
    {
        PlayerManager.getPlayers().forEach { addMoney(it, moneyPerInterval) }
    }

    override fun reset()
    {
    }
}