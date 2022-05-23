package de.jagenka.timer

import de.jagenka.DGPlayerManager
import net.minecraft.server.network.ServerPlayerEntity

object InactivePlayersTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val runEvery: Int
        get() = 1.ticks()

    private val inactiveTimer = mutableMapOf<ServerPlayerEntity, Int>().withDefault { 0 } //TODO: highlight inactive players

    override fun run()
    {
        DGPlayerManager.getInGamePlayers().forEach { inactiveTimer[it] = inactiveTimer.getValue(it) + 1 }
    }

    override fun reset()
    {
        inactiveTimer.clear()
    }
}