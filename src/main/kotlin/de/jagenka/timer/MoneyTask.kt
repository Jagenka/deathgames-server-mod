package de.jagenka.timer

import de.jagenka.DeathGames.currentlyEnding
import de.jagenka.config.Config
import de.jagenka.managers.BonusManager
import de.jagenka.managers.DisplayManager
import de.jagenka.managers.MoneyManager.addMoney
import de.jagenka.managers.PlayerManager

object MoneyTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val isGameMechanic: Boolean
        get() = true
    override val runEvery: Int
        get() = 1.ticks()

    private val moneyTimer = mutableMapOf<String, Int>().withDefault { 0 }

    val moneyInterval
        get() = Config.money.interval
    val moneyPerInterval
        get() = Config.money.amountPerInterval

    override fun run()
    {
        if (currentlyEnding) return

        PlayerManager.getParticipatingPlayers().forEach { playerName ->
            val playerTimer = moneyTimer.getValue(playerName)

            if (playerTimer >= moneyInterval)
            {
                addMoney(playerName, moneyPerInterval)
                moneyTimer[playerName] = playerTimer - moneyInterval
            }

            if (moneyPerInterval != 0 && !BonusManager.isOnActivePlatform(playerName))
            {
                DisplayManager.setExpProgress(playerName, playerTimer.toFloat() / moneyInterval.toFloat())
            }
            // else -> BonusMoneyTask

            moneyTimer[playerName] = moneyTimer.getValue(playerName) + 1

            // update account balance average moved to AverageBalance Task (needs to run when starting in shop)
        }
    }

    override fun reset()
    {
    }
}