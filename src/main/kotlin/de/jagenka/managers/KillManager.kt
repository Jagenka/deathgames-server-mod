package de.jagenka.managers

import de.jagenka.DeathGames
import de.jagenka.config.Config.respawnsPerPlayer
import de.jagenka.config.Config.respawnsPerTeam
import de.jagenka.gameplay.traps.TrapsAreNotGay
import de.jagenka.managers.DisplayManager.sendPrivateMessage
import de.jagenka.managers.PlayerManager.eliminate
import de.jagenka.managers.PlayerManager.getDGTeam
import de.jagenka.managers.PlayerManager.makeParticipating
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

    private val kDEntries = mutableMapOf<String, KD>()

    var livesMode = Mode.TEAM
    var killStreakMode = Mode.PLAYER


    @JvmStatic
    fun handleDeath(deceased: ServerPlayerEntity)
    {
        val playerName = deceased.name.string

        PlayerManager.registerAsCurrentlyDead(playerName)

        TrapsAreNotGay.onPlayerDeath(playerName)

        Timer.schedule({
            if (PlayerManager.requestRespawn(deceased))
            {
                deceased.sendPrivateMessage(I18n.get("forceRespawned"))
            }
        }, 5.seconds())

        if (!DeathGames.running) return

        val playerKD = kDEntries[playerName]
        if (playerKD != null) playerKD.addDeath()
        else kDEntries[playerName] = KD(0, 1)

        removeOneRespawn(deceased)

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
        val configString = I18n.get("shutdown", mapOf("killStreak" to killStreak, "deceased" to "%deceased")) //TODO kann man das ändern?
        return DisplayManager.getTextWithPlayersAndTeamsColored(configString, idToPlayer = mapOf("%deceased" to deceasedName))
    }

    @JvmStatic
    fun handlePlayerKill(attacker: ServerPlayerEntity, deceased: ServerPlayerEntity)
    {
        if (!DeathGames.running) return

        if (attacker == deceased) return
        if (attacker.getDGTeam() == deceased.getDGTeam()) return

        val attackerName = attacker.name.string

        val playerKD = kDEntries[attackerName]
        if (playerKD != null) playerKD.addKill()
        else kDEntries[attackerName] = KD(1, 0)

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
            Mode.PLAYER -> players.forEach { playerRespawns[it] = respawnsPerPlayer }
            Mode.TEAM -> players.forEach { teamRespawns[PlayerManager.getTeam(it)] = respawnsPerTeam }
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

        team.getOnlinePlayers().filter { !PlayerManager.isParticipating(it.name.string) }.randomOrNull()?.let { player ->
            player.makeParticipating()
            player.changeGameMode(GameMode.ADVENTURE)
            SpawnManager.teleportPlayerToSpawn(player)
            removeOneRespawn(player)
        }
    }

    fun getKDs() = kDEntries.toList().sortedByDescending { (_, kd) -> kd.getRatio() }

    fun reset()
    {
        playerRespawns.clear()
        teamRespawns.clear()
        playerKillStreak.clear()
        teamKillStreak.clear()
        kDEntries.clear()
    }
}

data class KD(var kills: Int, var deaths: Int)
{
    fun addKill() = kills++
    fun addDeath() = deaths++

    fun getRatio() = kills.toDouble() / deaths.coerceAtLeast(1).toDouble()

    override fun toString(): String
    {
        return "$kills / $deaths"
    }
}

enum class Mode
{
    PLAYER, TEAM
}