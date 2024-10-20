package de.jagenka.timer

import de.jagenka.DeathGames
import de.jagenka.config.Config
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
    override val isGameMechanic: Boolean
        get() = true
    override val runEvery: Int
        get() = 1.ticks()

    private val ticks = mutableMapOf<String, Int>().withDefault { 1 }

    val moneyInterval
        get() = Config.bonus.moneyInterval
    val moneyAmount
        get() = Config.bonus.moneyAmount

    override fun run()
    {
        // if bonus platforms are disabled, don't do anything
        if (!Config.bonus.enableBonusPlatforms) return

        if (DeathGames.currentlyEnding) return

        PlayerManager.getOnlinePlayers().forEach {
            val playerName = it.name.string
            if (ticks.getValue(playerName) >= moneyInterval)
            {
                addMoney(playerName, moneyAmount)
                ticks[playerName] = ticks.getValue(playerName) - moneyInterval
            }
            if (moneyAmount != 0 && BonusManager.isOnActivePlatform(playerName))
            {
                DisplayManager.setExpProgress(playerName, ticks.getValue(playerName).toFloat() / moneyInterval.toFloat())
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