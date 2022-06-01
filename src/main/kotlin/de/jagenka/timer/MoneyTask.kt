package de.jagenka.timer

import de.jagenka.Config
import de.jagenka.DGKillManager
import de.jagenka.DGPlayerManager

object MoneyTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val runEvery: Int
        get() = Config.moneyInterval

    override fun run()
    {
        DGPlayerManager.getPlayers().forEach { DGKillManager.addMoney(it, Config.moneyPerInterval) }
    }

    override fun reset()
    {
    }
}