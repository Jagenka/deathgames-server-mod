package de.jagenka.timer

import de.jagenka.config.Config.killStreakPenaltyCap
import de.jagenka.config.Config.revealTimePerPlayer
import de.jagenka.config.Config.shopCloseTimeAfterReveal
import de.jagenka.managers.DisplayManager
import de.jagenka.managers.KillManager
import de.jagenka.managers.PlayerManager
import net.minecraft.entity.boss.BossBar.Color.*
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
            val personalRevealTime = getPersonalRevealTime(playerName).toInt()
            val personalShopCloseTime = getPersonalShopCloseTime(playerName).toInt()

            PlayerManager.getOnlinePlayer(playerName)
                ?.let { player ->
                    if (time in 0..personalRevealTime)
                    {
                        val fillAmount = time.toDouble() / personalRevealTime.toDouble()
                        if (fillAmount < 0.75)
                        {
                            DisplayManager.setBossBarForPlayer(player, fillAmount.toFloat(), literal("Kill someone to prevent being revealed!"), GREEN)
                        } else if (fillAmount < 1)
                        {
                            DisplayManager.setBossBarForPlayer(player, fillAmount.toFloat(), literal("You are about to be revealed..."), YELLOW)
                        }
                    } else if (time in personalRevealTime + 1..personalRevealTime + personalShopCloseTime)
                    {
                        val fillAmount = (time - personalRevealTime).toDouble() / personalShopCloseTime.toDouble()
                        if (fillAmount < 0.75)
                        {
                            DisplayManager.setBossBarForPlayer(player, fillAmount.toFloat(), literal("You are glowing!"), RED)
                        } else if (fillAmount < 1)
                        {
                            DisplayManager.setBossBarForPlayer(player, fillAmount.toFloat(), literal("Shop is about to close for you!"), PINK)
                        } else
                        {
                            DisplayManager.setBossBarForPlayer(player, fillAmount.toFloat(), literal("Shop's closed!"), PURPLE)
                        }
                    }
                }

            if (time >= personalRevealTime)
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

    private fun getPersonalRevealTime(playerName: String) = revealTimePerPlayer.toDouble() * getKillStreakPenaltyFactor(playerName)
    private fun getPersonalShopCloseTime(playerName: String) = shopCloseTimeAfterReveal.toDouble() * getKillStreakPenaltyFactor(playerName)

    private fun getKillStreakPenaltyFactor(playerName: String) = (killStreakPenaltyCap - KillManager.getKillStreak(playerName)).toDouble() / killStreakPenaltyCap.toDouble()

    fun hasShopClosed(playerName: String) = (playerName in highlightedPlayers) && (inactiveTimer.getValue(playerName) >= (getPersonalRevealTime(playerName) * 2))

    fun resetForPlayer(name: String)
    {
        inactiveTimer[name] = 0
    }
}