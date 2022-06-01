package de.jagenka.timer

import de.jagenka.Config
import de.jagenka.managers.BonusManager
import de.jagenka.managers.MoneyManager.addMoney
import de.jagenka.managers.PlayerManager
import net.minecraft.server.network.ServerPlayerEntity

object BonusMoneyTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val runEvery: Int
        get() = 1.ticks()

    private val ticks = mutableMapOf<ServerPlayerEntity, Int>().withDefault { 1 }

    override fun run()
    {
        PlayerManager.getOnlinePlayers().forEach {
            if (ticks.getValue(it) % Config.bonusMoneyInterval == 0) addMoney(it.name.asString(), Config.bonusMoneyAmount)
            if (BonusManager.isOnActivePlatform(it)) ticks[it] = ticks.getValue(it) + 1
        }
    }

    override fun reset()
    {
        ticks.clear()
    }
}