package de.jagenka.timer

import de.jagenka.DGPlayerManager
import de.jagenka.addDGMoney

object MoneyTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val runEvery: Int
        get() = 30.seconds()

    override fun run()
    {
        DGPlayerManager.getPlayers().forEach { it.addDGMoney(5) } // TODO load from config
    }

    override fun reset()
    {
    }
}