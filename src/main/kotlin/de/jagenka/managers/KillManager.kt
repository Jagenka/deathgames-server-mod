package de.jagenka.managers

import de.jagenka.DeathGames
import de.jagenka.config.Config.livesPerPlayer
import de.jagenka.config.Config.livesPerTeam
import de.jagenka.config.Config.startMoneyPerPlayer
import de.jagenka.managers.DisplayManager.sendPrivateMessage
import de.jagenka.managers.MoneyManager.setMoney
import de.jagenka.managers.PlayerManager.eliminate
import de.jagenka.managers.PlayerManager.getDGTeam
import de.jagenka.managers.PlayerManager.makeInGame
import de.jagenka.team.DGTeam
import de.jagenka.timer.InactivePlayersTask
import de.jagenka.timer.Timer
import de.jagenka.timer.seconds
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.world.GameMode

object KillManager
{
    private val playerRespawns = mutableMapOf<String, Int>().withDefault { 0 }
    private val teamRespawns = mutableMapOf<DGTeam?, Int>().withDefault { 0 }

    private val playerKillStreak = mutableMapOf<String, Int>().withDefault { 0 }
    private val teamKillStreak = mutableMapOf<DGTeam?, Int>().withDefault { 0 }

    private val totalKills = mutableMapOf<String, Int>().withDefault { 0 }
    private val totalDeaths = mutableMapOf<String, Int>().withDefault { 0 }

    var moneyMode = Mode.PLAYER
    var livesMode = Mode.TEAM
    var killStreakMode = Mode.PLAYER


    @JvmStatic
    fun handleDeath(deceased: ServerPlayerEntity)
    {
        val playerName = deceased.name.string

        PlayerManager.registerAsCurrentlyDead(playerName)

        Timer.schedule({
            if (PlayerManager.requestRespawn(deceased))
            {
                deceased.sendPrivateMessage("You have been force-respawned.")
            }
        }, 5.seconds())

        if (!DeathGames.running) return

        totalDeaths[playerName] = totalDeaths.getValue(playerName) + 1
        removeOneRespawn(deceased)

        val killStreak = getKillStreak(playerName)
        if (killStreak >= 3)
        {
            val shutdownText = Text.literal("Shutdown! ")
            shutdownText.append(DisplayManager.getFormattedPlayerName(playerName))
            shutdownText.append(Text.of(" was on a kill streak of $killStreak."))
            DisplayManager.sendChatMessage(shutdownText)
        }

        resetKillStreak(deceased)

        DisplayManager.updateLivesDisplay()
        DisplayManager.updateKillStreakDisplay()

        InactivePlayersTask.resetForPlayer(playerName)
    }

    @JvmStatic
    fun handlePlayerKill(attacker: ServerPlayerEntity, deceased: ServerPlayerEntity)
    {
        if (!DeathGames.running) return

        if (attacker == deceased) return
        if (attacker.getDGTeam() == deceased.getDGTeam()) return

        totalKills[attacker.name.string] = totalKills.getValue(attacker.name.string) + 1
        when (killStreakMode)
        {
            Mode.PLAYER -> playerKillStreak[attacker.name.string] = playerKillStreak.getValue(attacker.name.string) + 1
            Mode.TEAM -> teamKillStreak[attacker.getDGTeam()] = teamKillStreak.getValue(attacker.getDGTeam()) + 1
        }
        MoneyManager.handleMoneyOnPlayerKill(attacker, deceased)
        InactivePlayersTask.resetForPlayer(attacker.name.string)

        DisplayManager.updateLivesDisplay()
        DisplayManager.updateKillStreakDisplay()
    }

    fun getKillStreak(playerName: String): Int
    {
        return when (killStreakMode)
        {
            Mode.PLAYER -> playerKillStreak.getValue(playerName)
            Mode.TEAM -> teamKillStreak.getValue(PlayerManager.getTeam(playerName))
        }
    }

    private fun resetKillStreak(deceased: ServerPlayerEntity)
    {
        when (killStreakMode)
        {
            Mode.PLAYER -> playerKillStreak[deceased.name.string] = 0
            Mode.TEAM -> teamKillStreak[deceased.getDGTeam()] = 0
        }

        DisplayManager.updateKillStreakDisplay()
    }

    fun removeOneRespawn(deceased: ServerPlayerEntity)
    {
        when (livesMode)
        {
            Mode.PLAYER ->
            {
                val respawnsAmount = playerRespawns.getValue(deceased.name.string)
                if (respawnsAmount > 0) playerRespawns[deceased.name.string] = respawnsAmount - 1
                if (respawnsAmount <= 0) deceased.eliminate()
            }
            Mode.TEAM ->
            {
                val respawnsAmount = teamRespawns.getValue(deceased.getDGTeam())
                if (respawnsAmount > 0) teamRespawns[deceased.getDGTeam()] = respawnsAmount - 1
                if (respawnsAmount <= 0) deceased.eliminate()
            }
        }

        DisplayManager.updateLivesDisplay()
    }

    fun initLives()
    {
        val players = PlayerManager.getPlayers()
        when (livesMode)
        {
            Mode.PLAYER -> players.forEach { playerRespawns[it] = livesPerPlayer }
            Mode.TEAM -> players.forEach { teamRespawns[PlayerManager.getTeam(it)] = livesPerTeam }
        }
    }

    fun initMoney()
    {
        val players = PlayerManager.getPlayers()

        when (moneyMode)
        {
            Mode.PLAYER -> players.forEach { setMoney(it, startMoneyPerPlayer) }
            Mode.TEAM ->
            {
                PlayerManager.getInGameTeams().forEach { inGameTeam ->
                    setMoney(inGameTeam, players.filter { playerName -> PlayerManager.getTeam(playerName) == inGameTeam }.size * startMoneyPerPlayer)
                }
            }
        }
    }

    fun getRespawns(playerName: String) = playerRespawns[playerName]
    fun getRespawns(team: DGTeam) = teamRespawns[team]

    fun addLives(playerName: String, amount: Int)
    {
        when (livesMode)
        {
            Mode.PLAYER -> playerRespawns[playerName] = playerRespawns.getValue(playerName) + amount
            Mode.TEAM ->
            {
                val team = PlayerManager.getTeam(playerName)
                teamRespawns[team] = teamRespawns.getValue(team) + amount
                team?.let { tryToRespawnDeadTeamPlayer(it) }
            }
        }
        DisplayManager.updateLivesDisplay()
    }

    fun tryToRespawnDeadTeamPlayer(team: DGTeam)
    {
        getRespawns(team)?.let { if (it < 1) return } ?: return

        team.getOnlinePlayers().filter { !PlayerManager.isInGame(it.name.string) }.randomOrNull()?.let { player ->
            player.makeInGame()
            player.changeGameMode(GameMode.ADVENTURE)
            SpawnManager.teleportPlayerToSpawn(player)
            removeOneRespawn(player)
        }
    }

    fun getNonZeroLifePlayers() = playerRespawns.filter { it.value > 0 }

    fun getNonZeroLifeTeams() = teamRespawns.filter { it.value > 0 }

    fun reset()
    {
        playerRespawns.clear()
        teamRespawns.clear()
        playerKillStreak.clear()
        teamKillStreak.clear()
        totalKills.clear()
        totalDeaths.clear()
    }
}

enum class Mode
{
    PLAYER, TEAM
}