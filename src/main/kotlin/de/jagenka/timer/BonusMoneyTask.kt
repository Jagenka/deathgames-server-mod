package de.jagenka.timer

import de.jagenka.*
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
        DGPlayerManager.getPlayers().forEach {
            if (ticks.getValue(it) % Config.bonusMoneyInterval == 0) it.addDGMoney(Config.bonusMoneyAmount)
            if (DGBonusManager.isOnActivePlatform(it)) ticks[it] = ticks.getValue(it) + 1
        }
    }

    override fun reset()
    {
        ticks.clear()
    }
}