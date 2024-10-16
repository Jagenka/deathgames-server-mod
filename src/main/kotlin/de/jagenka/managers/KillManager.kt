package de.jagenka.managers

import de.jagenka.DeathGames
import de.jagenka.config.Config
import de.jagenka.gameplay.traps.TrapsAreNotGay
import de.jagenka.managers.DisplayManager.sendPrivateMessage
import de.jagenka.managers.PlayerManager.getDGTeam
import de.jagenka.managers.PlayerManager.getTeam
import de.jagenka.stats.StatManager
import de.jagenka.stats.gib
import de.jagenka.team.DGTeam
import de.jagenka.timer.InactivePlayersTask
import de.jagenka.timer.Timer
import de.jagenka.timer.seconds
import de.jagenka.util.I18n
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.world.GameMode

object KillManager
{
    private val playerRespawns = mutableMapOf<String, Int>().withDefault { 0 }
    private val teamRespawns = mutableMapOf<DGTeam?, Int>().withDefault { 0 }

    private val playerKillStreak = mutableMapOf<String, Int>().withDefault { 0 }
    private val teamKillStreak = mutableMapOf<DGTeam?, Int>().withDefault { 0 }

    var livesMode = Mode.TEAM
    var killStreakMode = Mode.PLAYER


    @JvmStatic
    fun handleDeath(deceased: ServerPlayerEntity)
    {
        val playerName = deceased.name.string

        PlayerManager.registerAsCurrentlyDead(playerName)

        TrapsAreNotGay.onPlayerDeath(playerName)

        Timer.schedule(5.seconds()) {
            if (PlayerManager.requestRespawn(deceased))
            {
                deceased.sendPrivateMessage(I18n.get("forceRespawned"))
            }
        }

        if (!DeathGames.running) return

        removeOneRespawn(deceased.name.string)

        val killStreak = getKillStreak(playerName)
        if (killStreak >= 3)
        {
            DisplayManager.sendChatMessage(getShutdownText(playerName, killStreak))
        }

        resetKillStreak(deceased)

        DisplayManager.updateLivesDisplay()
        DisplayManager.updateKillStreakDisplay()

        InactivePlayersTask.resetForPlayer(playerName)
    }

    private fun getShutdownText(deceasedName: String, killStreak: Int): Text
    {
        // first get translated String from I18n, but keep deceased as a placeholder
        val configString = I18n.get("shutdown", mapOf("killStreak" to killStreak, "deceased" to "%deceased"))
        // to then replace the placeholder with colored text
        return DisplayManager.getTextWithPlayersAndTeamsColored(configString, idToPlayer = mapOf("%deceased" to deceasedName))
    }

    @JvmStatic
    fun handlePlayerKill(attacker: ServerPlayerEntity, deceased: ServerPlayerEntity)
    {
        if (!DeathGames.running) return

        if (attacker == deceased) return
        if (attacker.getDGTeam() == deceased.getDGTeam()) return

        val attackerName = attacker.name.string

        when (killStreakMode)
        {
            Mode.PLAYER -> playerKillStreak[attackerName] = playerKillStreak.getValue(attackerName) + 1
            Mode.TEAM -> teamKillStreak[attacker.getDGTeam()] = teamKillStreak.getValue(attacker.getDGTeam()) + 1
        }
        if (getKillStreak(attackerName) > StatManager.personalStats.gib(attackerName).highestKillStreak)
        {
            StatManager.personalStats.gib(attackerName).highestKillStreak = getKillStreak(attackerName)
        }

        MoneyManager.handleMoneyOnPlayerKill(attacker, deceased)
        InactivePlayersTask.resetForPlayer(attackerName)

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
        val playerName = deceased.name.string

        when (killStreakMode)
        {
            Mode.PLAYER -> playerKillStreak[playerName] = 0
            Mode.TEAM -> teamKillStreak[deceased.getDGTeam()] = 0
        }

        DisplayManager.updateKillStreakDisplay()
    }

    fun removeOneRespawn(playerName: String)
    {
        when (livesMode)
        {
            Mode.PLAYER ->
            {
                val respawnsAmount = playerRespawns.getValue(playerName)
                if (respawnsAmount > 0) playerRespawns[playerName] = respawnsAmount - 1
                if (respawnsAmount <= 0) PlayerManager.eliminate(playerName)
            }

            Mode.TEAM ->
            {
                val respawnsAmount = teamRespawns.getValue(getTeam(playerName))
                if (respawnsAmount > 0) teamRespawns[getTeam(playerName)] = respawnsAmount - 1
                if (respawnsAmount <= 0) PlayerManager.eliminate(playerName)
            }
        }

        DisplayManager.updateLivesDisplay()
    }

    fun initLives()
    {
        val players = PlayerManager.getPlayers()
        when (livesMode)
        {
            Mode.PLAYER -> players.forEach { playerRespawns[it] = Config.respawns.perTeam }
            Mode.TEAM -> players.forEach { teamRespawns[PlayerManager.getTeam(it)] = Config.respawns.perTeam }
        }
    }

    fun getRespawns(playerName: String) = playerRespawns[playerName]
    fun getRespawns(team: DGTeam) = teamRespawns[team]
    fun getRespawns(player: ServerPlayerEntity): Int?
    {
        return when (livesMode)
        {
            Mode.PLAYER -> getRespawns(player.name.string)
            Mode.TEAM -> getRespawns(player.getDGTeam() ?: return null)
        }
    }

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

        team.getOnlinePlayers().filter { !PlayerManager.isParticipating(it.name.string) }.randomOrNull()?.let { player ->
            PlayerManager.addParticipant(player.name.string)
            player.changeGameMode(GameMode.ADVENTURE)
            SpawnManager.teleportPlayerToSpawn(player)
            removeOneRespawn(player.name.string)
        }
    }

    fun reset()
    {
        playerRespawns.clear()
        teamRespawns.clear()
        playerKillStreak.clear()
        teamKillStreak.clear()
    }
}

enum class Mode
{
    PLAYER, TEAM
}