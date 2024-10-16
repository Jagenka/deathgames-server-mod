package de.jagenka.managers


import de.jagenka.DeathGames
import de.jagenka.Util
import de.jagenka.Util.ifServerLoaded
import de.jagenka.team.DGTeam
import de.jagenka.timer.ticks
import de.jagenka.util.I18n
import net.minecraft.entity.boss.BossBar
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket
import net.minecraft.network.packet.s2c.play.TitleS2CPacket
import net.minecraft.scoreboard.ScoreHolder
import net.minecraft.scoreboard.ScoreboardCriterion
import net.minecraft.scoreboard.ScoreboardDisplaySlot
import net.minecraft.scoreboard.ScoreboardObjective
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import java.util.regex.Pattern

object DisplayManager
{
    fun reset()
    {
        ifServerLoaded { server ->
            try
            {
                server.scoreboard.addObjective("sidebar", ScoreboardCriterion.DUMMY, Text.of(I18n.get("respawns")), ScoreboardCriterion.DUMMY.defaultRenderType, false, null)
            } catch (_: IllegalArgumentException)
            {
                DeathGames.logger.info("sidebar objective already exists")
            }
            // server.scoreboard.setObjectiveSlot(Scoreboard.SIDEBAR_DISPLAY_SLOT_ID, getObjective("sidebar"))

            try
            {
                server.scoreboard.addObjective("tabList", ScoreboardCriterion.DUMMY, Text.of(I18n.get("kill-streak")), ScoreboardCriterion.DUMMY.defaultRenderType, false, null)
            } catch (_: IllegalArgumentException)
            {
                DeathGames.logger.info("tabList objective already exists")
            }
            server.scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.LIST, getObjective("tabList"))


        }

        resetLevelDisplay()
        resetBossBars()
        resetKillStreakDisplay()
    }

    fun getObjective(name: String): ScoreboardObjective
    {
        Util.minecraftServer?.let { server ->
            return server.scoreboard.getNullableObjective(name) ?: return@let
        }
        error("objective $name missing")
    }

    fun prepareTeams()
    {
        ifServerLoaded { server ->
            DGTeam.entries.forEach { color ->
                server.scoreboard.addTeam(color.name + "_display")
                val team = server.scoreboard.getTeam(color.name + "_display")
                team?.color = Formatting.byName(color.name.lowercase())
                server.scoreboard.addScoreHolderToTeam(color.getPrettyName(), team)
            }
        }
    }

    fun updateLivesDisplay()
    {
        val sidebar = getObjective("sidebar")

        ifServerLoaded { server ->
            when (KillManager.livesMode)
            {
                Mode.PLAYER ->
                {
                    PlayerManager.getPlayers().forEach { playerName ->

                        val lives = KillManager.getRespawns(playerName)
                        if (lives != null && PlayerManager.isParticipating(playerName) && lives >= 0)
                        {
                            server.scoreboard.getOrCreateScore(
                                ScoreHolder.fromName(playerName),
                                sidebar
                            ).score = lives
                        } else
                        {
                            server.scoreboard.removeScore(ScoreHolder.fromName(playerName), sidebar)
                        }
                    }
                }

                Mode.TEAM ->
                {
                    DGTeam.entries.forEach { team ->
                        val lives = KillManager.getRespawns(team)
                        if (lives != null && PlayerManager.isParticipating(team) && lives >= 0)
                        {
                            server.scoreboard.getOrCreateScore(
                                ScoreHolder.fromName(team.getPrettyName()),
                                sidebar
                            ).score = lives
                        } else server.scoreboard.removeScore(ScoreHolder.fromName(team.getPrettyName()), sidebar)
                    }
                }
            }
        }
    }

    fun updateKillStreakDisplay()
    {
        val tabListObjective = getObjective("tabList")
        ifServerLoaded { server ->
            PlayerManager.getPlayers().forEach { playerName ->
                val killStreak = KillManager.getKillStreak(playerName)
                server.scoreboard.getOrCreateScore(ScoreHolder.fromName(playerName), tabListObjective).score = killStreak
            }
        }
    }

    fun resetKillStreakDisplay()
    {
        val tabListObjective = getObjective("tabList")
        ifServerLoaded { server ->
            PlayerManager.getPlayers().forEach { playerName ->
                server.scoreboard.getOrCreateScore(ScoreHolder.fromName(playerName), tabListObjective).score = 0
            }
        }
    }

    fun showSidebar()
    {
        updateLivesDisplay()
        Util.minecraftServer?.scoreboard?.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, getObjective("sidebar"))
            ?: DeathGames.logger.error("minecraft server not initialized")
    }

    fun hideSidebar()
    {
        Util.minecraftServer?.scoreboard?.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, null)
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
            player.setExperienceLevel(getDGMoney(player.name.string))
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
            it.playerManager.broadcast(text, false)
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
            sendChatMessage(Text.of(I18n.get("playerLeaveTeam", mapOf("playerName" to player.name.string))))
        } else
        {
            // first get translated String from I18n, but keep teamName as a placeholder
            val baseString = I18n.get("playerJoinTeam", mapOf("playerName" to player.name.string, "teamName" to "%teamName"))
            // to then replace the placeholder with colored text
            sendChatMessage(getTextWithPlayersAndTeamsColored(baseString, idToTeam = mapOf("%teamName" to team)))
        }
    }

    fun setBossBarForPlayer(player: ServerPlayerEntity, fillAmount: Float, text: Text, color: BossBar.Color, idSuffix: String = "main")
    {
        ifServerLoaded { server ->
            val bossBarId = Identifier.of(player.name.string.lowercase() + "_$idSuffix")
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
            val bossBarId = Identifier.of(player.name.string.lowercase() + "_$idSuffix")
            server.bossBarManager.get(bossBarId)?.let {
                it.removePlayer(player)
                server.bossBarManager.remove(it)
            }
        }
    }

    fun resetBossBars()
    {
        ifServerLoaded { server ->
            server.bossBarManager.ids.toList().forEach { id ->
                val bossBar = server.bossBarManager.get(id)
                bossBar?.clearPlayers()
                server.bossBarManager.remove(bossBar)
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

    fun getTextWithPlayersAndTeamsColored(string: String, idToPlayer: Map<String, String> = emptyMap(), idToTeam: Map<String, DGTeam> = emptyMap()): Text
    {
        val textAndSubStringIndexRange = mutableListOf<Pair<Text, Pair<Int, Int>>>()

        idToPlayer.forEach { (id, playerName) ->
            val matcher = Pattern.compile(id).matcher(string)
            while (matcher.find())
            {
                textAndSubStringIndexRange.add(
                    getFormattedPlayerName(playerName) to (matcher.start() to matcher.end())
                )
            }
        }

        idToTeam.forEach { (id, team) ->
            val matcher = Pattern.compile(id).matcher(string)
            while (matcher.find())
            {
                textAndSubStringIndexRange.add(team.getFormattedText() to (matcher.start() to matcher.end()))
            }
        }

        textAndSubStringIndexRange.sortBy { it.second.first }

        val base = Text.literal("")

        var currentIndex = 0
        for (i in 0 until textAndSubStringIndexRange.size)
        {
            val currentEntry = textAndSubStringIndexRange[i]
            base.append(Text.of(string.substring(currentIndex until currentEntry.second.first)))
            base.append(currentEntry.first)
            currentIndex = currentEntry.second.second
        }
        base.append(Text.of(string.substring(currentIndex until string.length)))

        return base
    }
}