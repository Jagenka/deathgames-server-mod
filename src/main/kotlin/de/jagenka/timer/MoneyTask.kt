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

    private val moneyTimer = mutableMapOf<String, Int>().withDefault { 0 }

    override fun run()
    {
        PlayerManager.getInGamePlayers().forEach { playerName ->
            val playerTimer = moneyTimer.getValue(playerName)

            if (playerTimer >= moneyInterval)
            {
                addMoney(playerName, moneyPerInterval)
                moneyTimer[playerName] = playerTimer - moneyInterval
            }

            if (!BonusManager.isOnActivePlatform(playerName))
            {
                DisplayManager.setExpProgress(playerName, playerTimer.toFloat() / moneyInterval.toFloat())
            }
            // else -> BonusMoneyTask

            moneyTimer[playerName] = moneyTimer.getValue(playerName) + 1
        }
    }

    override fun reset()
    {
    }
}