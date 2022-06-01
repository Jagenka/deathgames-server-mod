package de.jagenka.timer

import de.jagenka.Config
import de.jagenka.managers.MoneyManager.addMoney
import de.jagenka.managers.PlayerManager

object MoneyTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val runEvery: Int
        get() = Config.moneyInterval

    override fun run()
    {
        PlayerManager.getPlayers().forEach { addMoney(it, Config.moneyPerInterval) }
    }

    override fun reset()
    {
    }
}