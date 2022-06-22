package de.jagenka.managers


import de.jagenka.Util.ifServerLoaded
import de.jagenka.team.DGTeam
import de.jagenka.timer.ticks
import net.minecraft.entity.boss.BossBar
import net.minecraft.network.message.MessageType
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket
import net.minecraft.network.packet.s2c.play.TitleS2CPacket
import net.minecraft.scoreboard.Scoreboard
import net.minecraft.scoreboard.ScoreboardCriterion
import net.minecraft.scoreboard.ScoreboardObjective
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

object DisplayManager
{
    private lateinit var sidebarObjective: ScoreboardObjective
    private lateinit var tabListObjective: ScoreboardObjective

    fun reset()
    {
        ifServerLoaded { server ->
            if (!DisplayManager::sidebarObjective.isInitialized) sidebarObjective =
                ScoreboardObjective(server.scoreboard, "sidebar", ScoreboardCriterion.DUMMY, Text.of("Respawns"), ScoreboardCriterion.RenderType.INTEGER)
            server.scoreboard.objectives.toList().forEach { server.scoreboard.removeObjective(it) }
            server.scoreboard.addScoreboardObjective(sidebarObjective)

            if (!DisplayManager::tabListObjective.isInitialized) tabListObjective =
                ScoreboardObjective(server.scoreboard, "tabList", ScoreboardCriterion.DUMMY, Text.of("Kill-streak"), ScoreboardCriterion.RenderType.INTEGER)
            server.scoreboard.addScoreboardObjective(tabListObjective)
            server.scoreboard.setObjectiveSlot(Scoreboard.LIST_DISPLAY_SLOT_ID, tabListObjective)
        }

        resetLevelDisplay()
        resetBossBars()
        resetKillStreakDisplay()
    }

    fun prepareTeams()
    {
        ifServerLoaded { server ->
            DGTeam.values().forEach { color ->
                server.scoreboard.addTeam(color.name + "_display")
                val team = server.scoreboard.getTeam(color.name + "_display")
                team?.color = Formatting.byName(color.name.lowercase())
                server.scoreboard.addPlayerToTeam(color.getPrettyName(), team)
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
                        val lives = KillManager.getRespawns(playerName)
                        if (lives != null && PlayerManager.isInGame(playerName) && lives >= 0) server.scoreboard.getPlayerScore(playerName, sidebarObjective).score = lives
                        else server.scoreboard.resetPlayerScore(playerName, sidebarObjective)
                    }
                }
                Mode.TEAM ->
                {
                    DGTeam.values().forEach { team ->
                        val lives = KillManager.getRespawns(team)
                        if (lives != null && PlayerManager.isInGame(team) && lives >= 0) server.scoreboard.getPlayerScore(team.getPrettyName(), sidebarObjective).score = lives
                        else server.scoreboard.resetPlayerScore(team.getPrettyName(), sidebarObjective)
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

    fun resetKillStreakDisplay()
    {
        ifServerLoaded { server ->
            PlayerManager.getPlayers().forEach { playerName ->
                server.scoreboard.getPlayerScore(playerName, tabListObjective).score = 0
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

    fun setExpProgress(playerName: String, progress: Float)
    {
        PlayerManager.getOnlinePlayer(playerName)?.let { player ->
            player.setExperiencePoints((progress.coerceAtLeast(0f).coerceAtMost(1f) * player.nextLevelExperience).toInt())
        }
    }

    fun showTimeToBonusMessage(text: Text)
    {
        sendMessageToHotbar(text)
    }

    fun sendMessageToHotbar(text: Text, remainingFor: Int = 5.ticks())
    {
        PlayerManager.getOnlinePlayers().forEach { player ->
//            player.networkHandler.sendPacket(TitleFadeS2CPacket(0, remainingFor, 5)) raus weil konflikt mit sendTitleMessage
            player.networkHandler.sendPacket(OverlayMessageS2CPacket(text))
        }
    }

    fun sendTitleMessage(player: ServerPlayerEntity, title: Text, subtitle: Text, remainingFor: Int)
    {
        player.networkHandler.sendPacket(TitleFadeS2CPacket(5, remainingFor, 5))
        player.networkHandler.sendPacket(SubtitleS2CPacket(subtitle))
        player.networkHandler.sendPacket(TitleS2CPacket(title))
    }

    fun sendChatMessage(message: String)
    {
        sendChatMessage(Text.of(message))
    }

    fun sendChatMessage(text: Text)
    {
        ifServerLoaded {
            it.playerManager.broadcast(text, MessageType.TELLRAW_COMMAND)
        }
    }

    fun ServerPlayerEntity.sendPrivateMessage(text: String)
    {
        this.sendMessage(Text.of(text))
    }

    fun displayMessageOnPlayerTeamJoin(player: ServerPlayerEntity, team: DGTeam?)
    {
        if (team == null)
        {
            sendChatMessage(Text.of("${player.name.string} wants to spectate."))
        } else
        {
            val base = Text.literal("")
            base.append(Text.of("${player.name.string} joined Team "))
            base.append(team.getFormattedText())
            sendChatMessage(base)
        }
    }

    fun setBossBarForPlayer(player: ServerPlayerEntity, fillAmount: Float, text: Text, color: BossBar.Color, idSuffix: String = "main")
    {
        ifServerLoaded { server ->
            val bossBarId = Identifier(player.name.string.lowercase() + "_$idSuffix")
            var bossBar = server.bossBarManager.get(bossBarId)
            if (bossBar == null)
            {
                bossBar = server.bossBarManager.add(bossBarId, Text.of(""))
            }

            bossBar?.let {
                it.addPlayer(player)
                it.percent = fillAmount
                it.color = color
                it.name = text
            }
        }
    }

    fun removeBossBarForPlayer(player: ServerPlayerEntity, idSuffix: String)
    {
        ifServerLoaded { server ->
            val bossBarId = Identifier(player.name.string.lowercase() + "_$idSuffix")
            server.bossBarManager.get(bossBarId)?.let {
                it.removePlayer(player)
                server.bossBarManager.remove(it)
            }
        }
    }

    fun resetBossBars()
    {
        ifServerLoaded { server ->
            server.bossBarManager.all.toList().forEach { commandBossBar ->
                commandBossBar.players.toList().forEach { player ->
                    commandBossBar.removePlayer(player)
                }
                server.bossBarManager.remove(commandBossBar)
            }
        }
    }

    fun getFormattedPlayerName(playerName: String): Text
    {
        val team = PlayerManager.getTeam(playerName)
        team?.let {
            return Text.of(playerName).getWithStyle(Style.EMPTY.withFormatting(Formatting.byName(team.name.lowercase())))[0]
        }
        return Text.of(playerName)
    }
}