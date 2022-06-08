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
                ScoreboardObjective(server.scoreboard, "sidebar", ScoreboardCriterion.DUMMY, Text.of("Lives"), ScoreboardCriterion.RenderType.INTEGER)
            server.scoreboard.objectives.toList().forEach { server.scoreboard.removeObjective(it) }
            server.scoreboard.addScoreboardObjective(sidebarObjective)

            if (!DisplayManager::tabListObjective.isInitialized) tabListObjective =
                ScoreboardObjective(server.scoreboard, "tabList", ScoreboardCriterion.DUMMY, Text.of("Kill-streak"), ScoreboardCriterion.RenderType.INTEGER)
            server.scoreboard.addScoreboardObjective(tabListObjective)
            server.scoreboard.setObjectiveSlot(Scoreboard.LIST_DISPLAY_SLOT_ID, tabListObjective)
        }

        resetLevelDisplay()
        resetBossBars()
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

    fun updateBossBarForPlayer(player: ServerPlayerEntity, percent: Int)
    {
        ifServerLoaded { server ->

            val bossBarId = Identifier(player.name.string.lowercase())
            var bossBar = server.bossBarManager.get(bossBarId)
            if (bossBar == null)
            {
                bossBar = server.bossBarManager.add(bossBarId, Text.of(""))
            }

            bossBar?.let {
                it.addPlayer(player)
                it.percent = (percent.toFloat() / 100f).coerceAtMost(1f)

                if (percent <= 33)
                {
                    it.color = BossBar.Color.GREEN
                    it.name = Text.of("Time to kill someone!")
                } else if (percent <= 66)
                {
                    it.color = BossBar.Color.YELLOW
                    it.name = Text.of("Better kill someone soon...")
                } else if (percent < 100)
                {
                    it.color = BossBar.Color.RED
                    it.name = Text.of("You are about to be revealed!")
                } else
                {
                    it.color = BossBar.Color.RED
                    it.name = Text.of("You can no longer hide! `o´")
                }
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
}