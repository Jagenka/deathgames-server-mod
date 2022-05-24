package de.jagenka.timer

import de.jagenka.Config
import de.jagenka.DGPlayerManager
import de.jagenka.addDGMoney

object MoneyTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val runEvery: Int
        get() = Config.moneyInterval

    override fun run()
    {
        DGPlayerManager.getPlayers().forEach { it.addDGMoney(Config.moneyPerInterval) }
    }

    override fun reset()
    {
    }
}