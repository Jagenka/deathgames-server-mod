package de.jagenka.timer

import de.jagenka.config.Config.revealTimePerPlayer
import de.jagenka.managers.PlayerManager
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects

object InactivePlayersTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val runEvery: Int
        get() = 1.ticks()

    private val inactiveTimer = mutableMapOf<String, Int>().withDefault { 0 }

    override fun run()
    {
        inactiveTimer.forEach { (playerName, time) ->
            if (time >= revealTimePerPlayer)
            {
                PlayerManager.getOnlinePlayer(playerName)?.addStatusEffect(StatusEffectInstance(StatusEffects.GLOWING, 2.seconds(), 0, false, false))
            }
        }
        PlayerManager.getInGamePlayers().forEach { inactiveTimer[it] = inactiveTimer.getValue(it) + 1 }
    }

    override fun reset()
    {
        inactiveTimer.clear()
    }

    fun resetForPlayer(name: String)
    {
        inactiveTimer[name] = 0
    }
}