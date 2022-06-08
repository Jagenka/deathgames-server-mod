package de.jagenka.timer

import de.jagenka.config.Config.revealTimePerPlayer
import de.jagenka.floor
import de.jagenka.managers.DisplayManager
import de.jagenka.managers.PlayerManager
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.Text.literal
import net.minecraft.util.Formatting

object InactivePlayersTask : TimerTask
{
    private val highlightedPlayers = mutableSetOf<String>()

    override val onlyInGame: Boolean
        get() = true
    override val runEvery: Int
        get() = 1.ticks()

    private val inactiveTimer = mutableMapOf<String, Int>().withDefault { 0 }

    override fun run()
    {
        inactiveTimer.forEach { (playerName, time) ->
            val percentage = ((time.toDouble() / revealTimePerPlayer.toDouble()) * 100).floor()

            PlayerManager.getOnlinePlayer(playerName)?.let { DisplayManager.updateBossBarForPlayer(it, percentage) }

            if (percentage >= 100)
            {
                if (playerName !in highlightedPlayers)
                {
                    val base = literal("")
                    base.append(Text.of(playerName).getWithStyle(Style.EMPTY.withColor(Formatting.byName(PlayerManager.getTeam(playerName)?.name?.lowercase())))[0])
                    base.append(Text.of(" is now glowing!"))
                    DisplayManager.sendChatMessage(base)
                }

                highlightedPlayers.add(playerName)
                PlayerManager.getOnlinePlayer(playerName)?.addStatusEffect(StatusEffectInstance(StatusEffects.GLOWING, 2.seconds(), 0, false, false))
            } else
            {
                highlightedPlayers.remove(playerName)
            }
        }
        PlayerManager.getInGamePlayers().filter { !PlayerManager.isCurrentlyDead(it) }.forEach { inactiveTimer[it] = inactiveTimer.getValue(it) + 1 }
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