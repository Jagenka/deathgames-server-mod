package de.jagenka.managers


import de.jagenka.DGTeam
import de.jagenka.Util
import de.jagenka.Util.ifServerLoaded
import net.minecraft.network.MessageType
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket
import net.minecraft.network.packet.s2c.play.TitleS2CPacket
import net.minecraft.scoreboard.Scoreboard
import net.minecraft.scoreboard.ScoreboardCriterion
import net.minecraft.scoreboard.ScoreboardObjective
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.*

object DisplayManager
{
    private lateinit var sidebarObjective: ScoreboardObjective
    private lateinit var tabListObjective: ScoreboardObjective

    fun reset()
    {
        ifServerLoaded { server ->
            if (!DisplayManager::sidebarObjective.isInitialized) sidebarObjective =
                ScoreboardObjective(server.scoreboard, "sidebar", ScoreboardCriterion.DUMMY, Text.of("Lives"), ScoreboardCriterion.RenderType.INTEGER)
            server.scoreboard.objectives.toList().forEach { server.scoreboard.removeObjective(it) }
            server.scoreboard.addScoreboardObjective(sidebarObjective)

            if (!DisplayManager::tabListObjective.isInitialized) tabListObjective =
                ScoreboardObjective(server.scoreboard, "tabList", ScoreboardCriterion.DUMMY, Text.of("Kill-streak"), ScoreboardCriterion.RenderType.INTEGER)
            server.scoreboard.addScoreboardObjective(tabListObjective)
            server.scoreboard.setObjectiveSlot(Scoreboard.LIST_DISPLAY_SLOT_ID, tabListObjective)
        }

        resetLevelDisplay()
    }

    fun prepareTeams()
    {
        ifServerLoaded { server ->
            DGTeam.values().forEach { color ->
                server.scoreboard.addTeam(color.name + "_display")
                val team = server.scoreboard.getTeam(color.name + "_display")
                team?.color = Formatting.byName(color.name.lowercase())
                server.scoreboard.addPlayerToTeam(color.name, team)
            }
        }
    }

    fun updateLivesDisplay()
    {
        ifServerLoaded { server ->
            when (KillManager.livesMode)
            {
                Mode.PLAYER ->
                {
                    PlayerManager.getPlayers().forEach { playerName ->
                        val lives = KillManager.getLives(playerName)
                        if (lives != null && lives > 0) server.scoreboard.getPlayerScore(playerName, sidebarObjective).score = lives
                        else server.scoreboard.resetPlayerScore(playerName, sidebarObjective)
                    }
                }
                Mode.TEAM ->
                {
                    DGTeam.values().forEach { team ->
                        val lives = KillManager.getLives(team)
                        if (lives != null && lives > 0) server.scoreboard.getPlayerScore(team.name, sidebarObjective).score = lives
                        else server.scoreboard.resetPlayerScore(team.name, sidebarObjective)
                    }
                }
            }
        }
    }

    fun updateKillStreakDisplay()
    {
        ifServerLoaded { server ->
            PlayerManager.getPlayers().forEach { playerName ->
                val killStreak = KillManager.getKillStreak(playerName)
                server.scoreboard.getPlayerScore(playerName, tabListObjective).score = killStreak
            }
        }
    }

    fun showSidebar()
    {
        ifServerLoaded { server ->
            updateLivesDisplay()
            server.scoreboard.setObjectiveSlot(Scoreboard.SIDEBAR_DISPLAY_SLOT_ID, sidebarObjective)
        }
    }

    private fun resetLevelDisplay()
    {
        PlayerManager.getOnlinePlayers().forEach { player ->
            player.setExperiencePoints(0)
            player.setExperienceLevel(0)
        }
    }

    fun updateLevelDisplay()
    {
        PlayerManager.getOnlinePlayers().forEach { player ->
            player.setExperiencePoints(0)
            player.setExperienceLevel(player.getDGMoney())
        }
    }

    fun showTimeToBonusMessage(text: Text)
    {
        sendMessageToHotbar(text)
    }

    private fun sendMessageToHotbar(text: Text)
    {
        PlayerManager.getOnlinePlayers().forEach { player ->
            player.networkHandler.sendPacket(OverlayMessageS2CPacket(text))
        }
    }

    fun sendTitleMessage(player: ServerPlayerEntity, title: Text, subtitle: Text, remainingFor: Int)
    {
        player.networkHandler.sendPacket(TitleFadeS2CPacket(5, remainingFor, 5))
        player.networkHandler.sendPacket(SubtitleS2CPacket(subtitle))
        player.networkHandler.sendPacket(TitleS2CPacket(title))
    }

    fun sendChatMessage(message: String, sender: UUID = Util.modUUID)
    {
        sendChatMessage(Text.of(message), sender)
    }

    fun sendChatMessage(text: Text, sender: UUID = Util.modUUID)
    {
        ifServerLoaded { it.playerManager.broadcast(text, MessageType.CHAT, sender) }
    }

    fun ServerPlayerEntity.sendPrivateMessage(text: String)
    {
        this.sendMessage(Text.of(text), MessageType.CHAT, Util.modUUID)
    }
}