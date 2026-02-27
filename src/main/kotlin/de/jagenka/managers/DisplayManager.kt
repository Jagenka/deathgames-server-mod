package de.jagenka.managers


import de.jagenka.DeathGames
import de.jagenka.Util
import de.jagenka.Util.ifServerLoaded
import de.jagenka.team.DGTeam
import de.jagenka.timer.ticks
import de.jagenka.util.I18n
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.Objective
import net.minecraft.world.scores.ScoreHolder
import net.minecraft.world.scores.criteria.ObjectiveCriteria
import java.util.regex.Pattern

object DisplayManager
{
    fun reset()
    {
        ifServerLoaded { server ->
            try
            {
                server.scoreboard.addObjective(
                    "sidebar",
                    ObjectiveCriteria.DUMMY,
                    Component.literal(I18n.get("respawns")),
                    ObjectiveCriteria.DUMMY.defaultRenderType,
                    false,
                    null
                )
            } catch (_: IllegalArgumentException)
            {
                DeathGames.logger.info("sidebar objective already exists")
            }
            // server.scoreboard.setObjectiveSlot(Scoreboard.SIDEBAR_DISPLAY_SLOT_ID, getObjective("sidebar"))

            try
            {
                server.scoreboard.addObjective(
                    "tabList",
                    ObjectiveCriteria.DUMMY,
                    Component.literal(I18n.get("kill-streak")),
                    ObjectiveCriteria.DUMMY.defaultRenderType,
                    false,
                    null
                )
            } catch (_: IllegalArgumentException)
            {
                DeathGames.logger.info("tabList objective already exists")
            }
            server.scoreboard.setDisplayObjective(DisplaySlot.LIST, getObjective("tabList"))


        }

        resetLevelDisplay()
        resetBossBars()
        resetKillStreakDisplay()
    }

    fun getObjective(name: String): Objective
    {
        Util.minecraftServer?.let { server ->
            return server.scoreboard.getObjective(name) ?: return@let
        }
        error("objective $name missing")
    }

    fun prepareTeams()
    {
        ifServerLoaded { server ->
            DGTeam.entries.forEach { color ->
                server.scoreboard.addPlayerTeam(color.name + "_display")
                val team = server.scoreboard.getPlayerTeam(color.name + "_display")
                team?.color = ChatFormatting.getByName(color.name.lowercase()) ?: ChatFormatting.WHITE
                server.scoreboard.addPlayerToTeam(color.getPrettyName(), team!!)
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
                            server.scoreboard.getOrCreatePlayerScore(
                                ScoreHolder.forNameOnly(playerName),
                                sidebar
                            ).set(lives)
                        } else
                        {
                            server.scoreboard.resetSinglePlayerScore(ScoreHolder.forNameOnly(playerName), sidebar)
                        }
                    }
                }

                Mode.TEAM ->
                {
                    DGTeam.entries.forEach { team ->
                        val lives = KillManager.getRespawns(team)
                        if (lives != null && PlayerManager.isParticipating(team) && lives >= 0)
                        {
                            server.scoreboard.getOrCreatePlayerScore(
                                ScoreHolder.forNameOnly(team.getPrettyName()),
                                sidebar
                            ).set(lives)
                        } else server.scoreboard.resetSinglePlayerScore(
                            ScoreHolder.forNameOnly(team.getPrettyName()),
                            sidebar
                        )
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
                server.scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly(playerName), tabListObjective)
                    .set(killStreak)
            }
        }
    }

    fun resetKillStreakDisplay()
    {
        val tabListObjective = getObjective("tabList")
        ifServerLoaded { server ->
            PlayerManager.getPlayers().forEach { playerName ->
                server.scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly(playerName), tabListObjective).set(0)
            }
        }
    }

    fun showSidebar()
    {
        updateLivesDisplay()
        Util.minecraftServer?.scoreboard?.setDisplayObjective(DisplaySlot.SIDEBAR, getObjective("sidebar"))
            ?: DeathGames.logger.error("minecraft server not initialized")
    }

    fun hideSidebar()
    {
        Util.minecraftServer?.scoreboard?.setDisplayObjective(DisplaySlot.SIDEBAR, null)
    }

    private fun resetLevelDisplay()
    {
        PlayerManager.getOnlinePlayers().forEach { player ->
            player.setExperiencePoints(0)
            player.setExperienceLevels(0)
        }
    }

    fun updateLevelDisplay()
    {
        PlayerManager.getOnlinePlayers().forEach { player ->
            player.setExperiencePoints(0)
            player.setExperienceLevels(getDGMoney(player.name.string))
        }
    }

    fun setExpProgress(playerName: String, progress: Float)
    {
        PlayerManager.getOnlinePlayer(playerName)?.let { player ->
            player.setExperiencePoints(
                (progress.coerceAtLeast(0f).coerceAtMost(1f) * player.xpNeededForNextLevel).toInt()
            )
        }
    }

    fun showTimeToBonusMessage(textComponent: Component)
    {
        sendMessageToHotbar(textComponent)
    }

    fun sendMessageToHotbar(textComponent: Component, remainingFor: Int = 5.ticks())
    {
        PlayerManager.getOnlinePlayers().forEach { player ->
//            player.networkHandler.sendPacket(TitleFadeS2CPacket(0, remainingFor, 5)) raus weil konflikt mit sendTitleMessage
            player.connection.send(ClientboundSetActionBarTextPacket(textComponent))
        }
    }

    fun sendTitleMessage(
        player: ServerPlayer,
        titleComponent: Component,
        subtitleComponent: Component,
        remainingFor: Int
    )
    {
        player.connection.send(ClientboundSetTitlesAnimationPacket(5, remainingFor, 5))
        player.connection.send(ClientboundSetSubtitleTextPacket(subtitleComponent))
        player.connection.send(ClientboundSetTitleTextPacket(titleComponent))
    }

    fun sendChatMessage(message: String)
    {
        sendChatMessage(Component.literal(message))
    }

    fun sendChatMessage(textComponent: Component)
    {
        ifServerLoaded {
            it.playerList.broadcastSystemMessage(textComponent, false)
        }
    }

    fun ServerPlayer.sendPrivateMessage(text: String)
    {
        this.sendSystemMessage(Component.literal(text))
    }

    fun displayMessageOnPlayerTeamJoin(player: ServerPlayer, team: DGTeam?)
    {
        if (team == null)
        {
            sendChatMessage(Component.literal(I18n.get("playerLeaveTeam", mapOf("playerName" to player.name.string))))
        } else
        {
            // first get translated String from I18n, but keep teamName as a placeholder
            val baseString = I18n.get("playerJoinTeam", mapOf("playerName" to player.name.string, "teamName" to "%teamName"))
            // to then replace the placeholder with colored text
            sendChatMessage(getTextWithPlayersAndTeamsColored(baseString, idToTeam = mapOf("%teamName" to team)))
        }
    }

    fun setBossBarForPlayer(
        player: ServerPlayer,
        fillAmount: Float,
        textComponent: Component,
        color: BossEvent.BossBarColor,
        idSuffix: String = "main"
    )
    {
        ifServerLoaded { server ->
            val bossBarId = Identifier.parse(player.name.string.lowercase() + "_$idSuffix")
            var bossBar = server.customBossEvents.get(bossBarId)
            if (bossBar == null)
            {
                bossBar = server.customBossEvents.create(bossBarId, Component.literal(""))
            }

            bossBar?.let {
                it.addPlayer(player)
                it.progress = fillAmount
                it.color = color
                it.name = textComponent
            }
        }
    }

    fun removeBossBarForPlayer(player: ServerPlayer, idSuffix: String)
    {
        ifServerLoaded { server ->
            val bossBarId = Identifier.parse(player.name.string.lowercase() + "_$idSuffix")
            server.customBossEvents.get(bossBarId)?.let {
                it.removePlayer(player)
                server.customBossEvents.remove(it)
            }
        }
    }

    fun resetBossBars()
    {
        ifServerLoaded { server ->
            server.customBossEvents.ids.toList().forEach { id ->
                val bossBar = server.customBossEvents.get(id)
                bossBar?.removeAllPlayers()
                server.customBossEvents.remove(bossBar!!)
            }
        }
    }

    fun getFormattedPlayerName(playerName: String): Component
    {
        val team = PlayerManager.getTeam(playerName)
        team?.let {
            return Component.literal(playerName).withColor(
                ChatFormatting.getByName(team.name.lowercase())?.color ?: 0
            )
        }
        return Component.literal(playerName)
    }

    fun getTextWithPlayersAndTeamsColored(
        string: String,
        idToPlayer: Map<String, String> = emptyMap(),
        idToTeam: Map<String, DGTeam> = emptyMap()
    ): Component
    {
        val textAndSubStringIndexRange = mutableListOf<Pair<Component, Pair<Int, Int>>>()

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

        val base = Component.literal("")

        var currentIndex = 0
        for (i in 0 until textAndSubStringIndexRange.size)
        {
            val currentEntry = textAndSubStringIndexRange[i]
            base.append(Component.literal(string.substring(currentIndex until currentEntry.second.first)))
            base.append(currentEntry.first)
            currentIndex = currentEntry.second.second
        }
        base.append(Component.literal(string.substring(currentIndex until string.length)))

        return base
    }
}