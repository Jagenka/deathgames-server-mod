package de.jagenka


import de.jagenka.Util.ifServerLoaded
import net.minecraft.scoreboard.Scoreboard
import net.minecraft.scoreboard.ScoreboardCriterion
import net.minecraft.scoreboard.ScoreboardObjective
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object DGDisplayManager
{
    private lateinit var sidebarObjective: ScoreboardObjective
    private lateinit var tabListObjective: ScoreboardObjective

    fun reset()
    {
        ifServerLoaded { server ->
            if (!DGDisplayManager::sidebarObjective.isInitialized) sidebarObjective =
                ScoreboardObjective(server.scoreboard, "sidebar", ScoreboardCriterion.DUMMY, Text.of("change this"), ScoreboardCriterion.RenderType.INTEGER)
            server.scoreboard.objectives.toList().forEach { server.scoreboard.removeObjective(it) }
            server.scoreboard.addScoreboardObjective(sidebarObjective)

            if (!DGDisplayManager::tabListObjective.isInitialized) tabListObjective =
                ScoreboardObjective(server.scoreboard, "tabList", ScoreboardCriterion.DUMMY, Text.of("change this"), ScoreboardCriterion.RenderType.INTEGER)
            server.scoreboard.addScoreboardObjective(tabListObjective)
            server.scoreboard.setObjectiveSlot(Scoreboard.LIST_DISPLAY_SLOT_ID, tabListObjective)
        }

        prepareTeams()
    }

    private fun prepareTeams()
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

    fun updateSidebar()
    {
        ifServerLoaded { server ->
            when (DGKillManager.livesMode)
            {
                Mode.PLAYER ->
                {
                    DGPlayerManager.getPlayers().forEach { player ->
                        val lives = DGKillManager.getLives(player)
                        if (lives != null && lives > 0) server.scoreboard.getPlayerScore(player.name.asString(), sidebarObjective).score = lives
                        else server.scoreboard.resetPlayerScore(player.name.asString(), sidebarObjective)
                    }
                }
                Mode.TEAM ->
                {
                    DGTeam.values().forEach { team ->
                        val lives = DGKillManager.getLives(team)
                        if (lives != null && lives > 0) server.scoreboard.getPlayerScore(team.name, sidebarObjective).score = lives
                        else server.scoreboard.resetPlayerScore(team.name, sidebarObjective)
                    }
                }
            }
        }
    }

    fun showSidebar()
    {
        ifServerLoaded { server ->
            updateSidebar()
            server.scoreboard.setObjectiveSlot(Scoreboard.SIDEBAR_DISPLAY_SLOT_ID, sidebarObjective)
        }
    }
}