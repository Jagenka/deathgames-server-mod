package de.jagenka.timer

import de.jagenka.DGTeam
import de.jagenka.DeathGames
import de.jagenka.managers.DisplayManager
import de.jagenka.managers.PlayerManager
import net.minecraft.text.Style
import net.minecraft.text.Text.literal
import net.minecraft.util.Formatting

object GameOverTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val runEvery: Int
        get() = 1.ticks()

    override fun run()
    {
        if (PlayerManager.getOnlineInGameTeams().size <= 1)
        {
            DeathGames.stopGame()
        }
    }

    fun handleTeamGameOver(team: DGTeam)
    {
        if (team !in PlayerManager.getInGameTeams())
        {
            val prefix = literal("Game Over for Team ")
            val teamText = literal("$team").getWithStyle(Style.EMPTY.withColor(Formatting.byName(team.name.lowercase())))[0]
            val suffix = literal(".")

            DisplayManager.sendChatMessage(prefix.append(teamText).append(suffix))
        }
    }

    override fun reset()
    {
    }
}