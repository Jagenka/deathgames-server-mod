package de.jagenka.timer

import de.jagenka.config.Config.bonusMoneyAmount
import de.jagenka.config.Config.bonusMoneyInterval
import de.jagenka.managers.BonusManager
import de.jagenka.managers.DisplayManager
import de.jagenka.managers.MoneyManager.addMoney
import de.jagenka.managers.PlayerManager
import de.jagenka.stats.StatManager
import de.jagenka.stats.gib

object BonusMoneyTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val runEvery: Int
        get() = 1.ticks()

    private val ticks = mutableMapOf<String, Int>().withDefault { 1 }

    override fun run()
    {
        PlayerManager.getOnlinePlayers().forEach {
            val playerName = it.name.string
            if (ticks.getValue(playerName) >= bonusMoneyInterval)
            {
                addMoney(playerName, bonusMoneyAmount)
                ticks[playerName] = ticks.getValue(playerName) - bonusMoneyInterval
            }
            if (bonusMoneyAmount != 0 && BonusManager.isOnActivePlatform(playerName))
            {
                DisplayManager.setExpProgress(playerName, ticks.getValue(playerName).toFloat() / bonusMoneyInterval.toFloat())
                ticks[playerName] = ticks.getValue(playerName) + 1

                StatManager.personalStats.gib(playerName).ticksOnBonus++
            }
        }
    }

    override fun reset()
    {
        ticks.clear()
    }
}