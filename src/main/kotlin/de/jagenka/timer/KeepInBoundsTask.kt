package de.jagenka.timer

import de.jagenka.config.Config
import de.jagenka.managers.PlayerManager
import de.jagenka.toDGCoordinates
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects

object KeepInBoundsTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val runEvery: Int
        get() = 1.ticks()

    override fun run()
    {
        PlayerManager.getOnlineInGamePlayers().forEach { player ->
            if (!Config.arenaBounds.contains(player.pos.toDGCoordinates()))
            {
                player.addStatusEffect(StatusEffectInstance(StatusEffects.WITHER, 1.seconds(), 9))
            }
        }
    }

    override fun reset()
    {

    }
}