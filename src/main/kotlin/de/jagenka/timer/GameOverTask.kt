package de.jagenka.timer

import de.jagenka.DeathGames
import de.jagenka.managers.DisplayManager
import de.jagenka.managers.PlayerManager
import de.jagenka.team.DGTeam
import net.minecraft.network.chat.Component
import net.minecraft.world.level.GameType

object GameOverTask : TimerTask
{
    private val participatingTeams = mutableSetOf<DGTeam>()

    private var gameEnded = false

    override val onlyInGame: Boolean
        get() = true
    override val isGameMechanic: Boolean
        get() = false
    override val runEvery: Int
        get() = 1.ticks()

    override fun run()
    {
        if (DeathGames.currentlyEnding)
        {
            PlayerManager.getOnlinePlayers().forEach { it.setGameMode(GameType.SPECTATOR) }
        }

        if (gameEnded) return

        val onlineParticipatingTeams = PlayerManager.getOnlineParticipatingTeams()
        onlineParticipatingTeams.toList().forEach { if (it !in participatingTeams) participatingTeams.add(it) }

        participatingTeams.toList().forEach {
            if (it !in onlineParticipatingTeams)
            {
                handleTeamGameOver(it)
                participatingTeams.remove(it)
            }
        }

        if (onlineParticipatingTeams.size <= 1)
        {
            DeathGames.stopGame()
            gameEnded = true
        }
    }

    fun handleTeamGameOver(team: DGTeam)
    {
        if (team !in PlayerManager.getParticipatingTeams())
        {
            val prefix = Component.literal("Game Over for Team ")
            val teamText = team.getFormattedText()
            val suffix = Component.literal(".")

            DisplayManager.sendChatMessage(prefix.append(teamText).append(suffix))
        }
    }

    override fun reset()
    {
        participatingTeams.clear()
        gameEnded = false
    }
}