package de.jagenka.timer

import de.jagenka.DeathGames
import de.jagenka.managers.DisplayManager
import de.jagenka.managers.PlayerManager
import de.jagenka.team.DGTeam
import net.minecraft.text.Style
import net.minecraft.text.Text.literal
import net.minecraft.util.Formatting

object GameOverTask : TimerTask
{
    private val inGameTeams = mutableSetOf<DGTeam>()

    private var gameEnded = false

    override val onlyInGame: Boolean
        get() = true
    override val runEvery: Int
        get() = 1.ticks()

    override fun run()
    {
        if (gameEnded) return

        val onlineInGameTeams = PlayerManager.getOnlineInGameTeams()
        onlineInGameTeams.toList().forEach { if (it !in inGameTeams) inGameTeams.add(it) }

        inGameTeams.toList().forEach {
            if (it !in onlineInGameTeams)
            {
                handleTeamGameOver(it)
                inGameTeams.remove(it)
            }
        }

        if (onlineInGameTeams.size <= 1)
        {
            DeathGames.stopGame()
            gameEnded = true
        }
    }

    fun handleTeamGameOver(team: DGTeam)
    {
        if (team !in PlayerManager.getInGameTeams())
        {
            val prefix = literal("Game Over for Team ")
            val teamText = team.getFormattedText()
            val suffix = literal(".")

            DisplayManager.sendChatMessage(prefix.append(teamText).append(suffix))
        }
    }

    override fun reset()
    {
        inGameTeams.clear()
        gameEnded = false
    }
}