package de.jagenka.timer

import de.jagenka.DeathGames.currentlyEnding
import de.jagenka.config.Config.killStreakPenaltyCap
import de.jagenka.config.Config.revealTimePerPlayer
import de.jagenka.config.Config.shopCloseTimeAfterReveal
import de.jagenka.managers.DisplayManager
import de.jagenka.managers.KillManager
import de.jagenka.managers.PlayerManager
import de.jagenka.util.I18n
import net.minecraft.entity.boss.BossBar.Color.*
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.text.Text.literal

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
        if (currentlyEnding) return

        if (Timer.gameMechsPaused) return

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
                            DisplayManager.setBossBarForPlayer(player, fillAmount.toFloat(), literal(I18n.get("revealTimer0")), GREEN, idSuffix = "reveal")
                        } else if (fillAmount < 1)
                        {
                            DisplayManager.setBossBarForPlayer(player, fillAmount.toFloat(), literal(I18n.get("revealTimer1")), YELLOW, idSuffix = "reveal")
                        }
                    } else if (time in personalRevealTime + 1..personalRevealTime + personalShopCloseTime)
                    {
                        val fillAmount = (time - personalRevealTime).toDouble() / personalShopCloseTime.toDouble()
                        if (fillAmount < 0.75)
                        {
                            DisplayManager.setBossBarForPlayer(player, fillAmount.toFloat(), literal(I18n.get("revealTimer2")), RED, idSuffix = "reveal")
                        } else if (fillAmount < 1)
                        {
                            DisplayManager.setBossBarForPlayer(player, fillAmount.toFloat(), literal(I18n.get("revealTimer3")), PINK, idSuffix = "reveal")
                        } else
                        {
                            DisplayManager.setBossBarForPlayer(player, fillAmount.toFloat(), literal(I18n.get("revealTimer4")), PURPLE, idSuffix = "reveal")
                        }
                    }
                }

            if (time >= personalRevealTime)
            {
                if (playerName !in highlightedPlayers)
                {
                    DisplayManager.sendChatMessage(
                        DisplayManager.getTextWithPlayersAndTeamsColored(
                            I18n.get("nowGlowing", mapOf("playerName" to "%playerName")),
                            mapOf("%playerName" to playerName)
                        )
                    )
                }

                highlightedPlayers.add(playerName)
                if(PlayerManager.getOnlinePlayer(playerName)?.activeStatusEffects?.contains(StatusEffects.INVISIBILITY) != true)
                {
                    PlayerManager.getOnlinePlayer(playerName)?.addStatusEffect(StatusEffectInstance(StatusEffects.GLOWING, 2.seconds(), 0, false, false))
                }
            } else
            {
                highlightedPlayers.remove(playerName)
            }
        }
        PlayerManager.getParticipatingPlayers().filter { !PlayerManager.isCurrentlyDead(it) }.forEach { inactiveTimer[it] = inactiveTimer.getValue(it) + 1 }
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