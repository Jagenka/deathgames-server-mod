package de.jagenka.timer

import de.jagenka.DeathGames.currentlyEnding
import de.jagenka.config.Config
import de.jagenka.managers.DisplayManager
import de.jagenka.managers.KillManager
import de.jagenka.managers.PlayerManager
import de.jagenka.util.I18n
import net.minecraft.network.chat.Component
import net.minecraft.world.BossEvent
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects

object InactivePlayersTask : TimerTask
{
    private val highlightedPlayers = mutableSetOf<String>()

    override val onlyInGame: Boolean
        get() = true
    override val isGameMechanic: Boolean
        get() = true
    override val runEvery: Int
        get() = 1.ticks()

    private val inactiveTimer = mutableMapOf<String, Int>().withDefault { 0 }

    override fun run()
    {
        // if reveal is disabled, don't do anything
        if (!Config.misc.enableReveal) return

        if (currentlyEnding) return

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
                            DisplayManager.setBossBarForPlayer(
                                player, fillAmount.toFloat(), Component.literal(I18n.get("revealTimer0")),
                                BossEvent.BossBarColor.GREEN, idSuffix = "reveal"
                            )
                        } else if (fillAmount < 1)
                        {
                            DisplayManager.setBossBarForPlayer(
                                player, fillAmount.toFloat(), Component.literal(I18n.get("revealTimer1")),
                                BossEvent.BossBarColor.YELLOW, idSuffix = "reveal"
                            )
                        }
                    } else if (time in personalRevealTime + 1..personalRevealTime + personalShopCloseTime)
                    {
                        val fillAmount = (time - personalRevealTime).toDouble() / personalShopCloseTime.toDouble()
                        if (fillAmount < 0.75)
                        {
                            DisplayManager.setBossBarForPlayer(
                                player, fillAmount.toFloat(), Component.literal(I18n.get("revealTimer2")),
                                BossEvent.BossBarColor.RED, idSuffix = "reveal"
                            )
                        } else if (fillAmount < 1)
                        {
                            DisplayManager.setBossBarForPlayer(
                                player, fillAmount.toFloat(), Component.literal(I18n.get("revealTimer3")),
                                BossEvent.BossBarColor.PINK, idSuffix = "reveal"
                            )
                        } else
                        {
                            DisplayManager.setBossBarForPlayer(
                                player, fillAmount.toFloat(), Component.literal(I18n.get("revealTimer4")),
                                BossEvent.BossBarColor.PURPLE, idSuffix = "reveal"
                            )
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
                if (PlayerManager.getOnlinePlayer(playerName)?.activeEffects?.contains(MobEffectInstance(MobEffects.INVISIBILITY)) != true)
                {
                    PlayerManager.getOnlinePlayer(playerName)
                        ?.addEffect(MobEffectInstance(MobEffects.GLOWING, 2.seconds(), 0, false, false))
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

    private fun getPersonalRevealTime(playerName: String) = Config.misc.revealTimePerPlayer.toDouble() * getKillStreakPenaltyFactor(playerName)
    private fun getPersonalShopCloseTime(playerName: String) = Config.misc.shopCloseTimeAfterReveal.toDouble() * getKillStreakPenaltyFactor(playerName)

    private fun getKillStreakPenaltyFactor(playerName: String) =
        (Config.misc.killStreakPenaltyCap - KillManager.getKillStreak(playerName)).toDouble() / Config.misc.killStreakPenaltyCap.toDouble()

    fun hasShopClosed(playerName: String) = (playerName in highlightedPlayers) && (inactiveTimer.getValue(playerName) >= (getPersonalRevealTime(playerName) * 2))

    fun resetForPlayer(name: String)
    {
        inactiveTimer[name] = 0
    }
}