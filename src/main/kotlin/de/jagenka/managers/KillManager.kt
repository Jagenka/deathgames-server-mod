package de.jagenka.managers

import de.jagenka.DGTeam
import de.jagenka.config.Config.livesPerPlayer
import de.jagenka.config.Config.livesPerTeam
import de.jagenka.config.Config.moneyBonusPerKillStreakKill
import de.jagenka.config.Config.moneyPerKill
import de.jagenka.config.Config.startMoneyPerPlayer
import de.jagenka.managers.DisplayManager.sendChatMessage
import de.jagenka.managers.DisplayManager.sendPrivateMessage
import de.jagenka.managers.MoneyManager.addMoney
import de.jagenka.managers.MoneyManager.setMoney
import de.jagenka.managers.PlayerManager.eliminate
import de.jagenka.managers.PlayerManager.getDGTeam
import de.jagenka.timer.GameOverTask
import de.jagenka.timer.InactivePlayersTask
import de.jagenka.timer.ShuffleSpawnsTask
import net.minecraft.entity.Entity
import net.minecraft.server.network.ServerPlayerEntity

object KillManager
{
    private val playerLives = mutableMapOf<String, Int>().withDefault { 0 }
    private val teamLives = mutableMapOf<DGTeam?, Int>().withDefault { 0 }

    private val playerKillStreak = mutableMapOf<String, Int>().withDefault { 0 }
    private val teamKillStreak = mutableMapOf<DGTeam?, Int>().withDefault { 0 }

    private val totalKills = mutableMapOf<String, Int>().withDefault { 0 }
    private val totalDeaths = mutableMapOf<String, Int>().withDefault { 0 }

    var moneyMode = Mode.PLAYER // TODO raus
    var livesMode = Mode.TEAM
    var killStreakMode = Mode.PLAYER


    @JvmStatic
    fun handleDeath(attacker: Entity?, deceased: ServerPlayerEntity)
    {
        if (attacker is ServerPlayerEntity)
        {
            sendChatMessage("${attacker.name.string} killed ${deceased.name.string}")
            totalKills[attacker.name.string] = totalKills.getValue(attacker.name.string) + 1
            when (killStreakMode)
            {
                Mode.PLAYER -> playerKillStreak[attacker.name.string] = playerKillStreak.getValue(attacker.name.string) + 1
                Mode.TEAM -> teamKillStreak[attacker.getDGTeam()] = teamKillStreak.getValue(attacker.getDGTeam()) + 1
            }
            handleMoney(attacker, deceased)
            InactivePlayersTask.resetForPlayer(attacker.name.string)
        }

        totalDeaths[deceased.name.string] = totalDeaths.getValue(deceased.name.string) + 1
        removeOneLife(deceased)
        resetKillStreak(deceased)

        PlayerManager.registerAsCurrentlyDead(deceased.name.string)

        DisplayManager.updateLivesDisplay()
        DisplayManager.updateKillStreakDisplay()

        deceased.getDGTeam()?.let { GameOverTask.handleTeamGameOver(it) }
        ShuffleSpawnsTask.updateLastKillTime()
        InactivePlayersTask.resetForPlayer(deceased.name.string)
    }

    private fun handleMoney(attacker: ServerPlayerEntity, deceased: ServerPlayerEntity) // TODO raus
    {
        when (moneyMode)
        {
            Mode.PLAYER ->
            {
                val killStreakAmount = getKillStreak(deceased.name.string)
                addMoney(attacker.name.string, moneyPerKill + moneyBonusPerKillStreakKill * killStreakAmount)
                sendChatMessage("They made $killStreakAmount kill${if (killStreakAmount != 1) "s" else ""} since their previous death.")
                attacker.sendPrivateMessage("You received ${moneyPerKill + moneyBonusPerKillStreakKill * killStreakAmount}")
            }
            Mode.TEAM ->
            {
                val killStreakAmount = getKillStreak(deceased.name.string)
                addMoney(attacker.getDGTeam(), moneyPerKill + moneyBonusPerKillStreakKill * killStreakAmount)
                sendChatMessage("${attacker.getDGTeam()?.name ?: "They"} made $killStreakAmount kill${if (killStreakAmount != 1) "s" else ""} since their previous death.")
                attacker.getDGTeam()?.getOnlinePlayers()?.forEach { it.sendPrivateMessage("Your team received ${moneyPerKill + moneyBonusPerKillStreakKill * killStreakAmount}") }
            }
        }

        DisplayManager.updateLevelDisplay()
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

    fun removeOneLife(deceased: ServerPlayerEntity)
    {
        when (livesMode)
        {
            Mode.PLAYER ->
            {
                val livesAmount = playerLives.getValue(deceased.name.string)
                if (livesAmount > 0) playerLives[deceased.name.string] = livesAmount - 1
                if (livesAmount - 1 < 1) deceased.eliminate()
            }
            Mode.TEAM ->
            {
                val livesAmount = teamLives.getValue(deceased.getDGTeam())
                if (livesAmount > 0) teamLives[deceased.getDGTeam()] = livesAmount - 1
                if (livesAmount - 1 < 1) deceased.eliminate()
            }
        }

        DisplayManager.updateLivesDisplay()
    }

    fun initLives()
    {
        val players = PlayerManager.getPlayers()
        when (livesMode)
        {
            Mode.PLAYER -> players.forEach { playerLives[it] = livesPerPlayer }
            Mode.TEAM -> players.forEach { teamLives[PlayerManager.getTeam(it)] = livesPerTeam }
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

    fun getLives(playerName: String) = playerLives[playerName]
    fun getLives(team: DGTeam) = teamLives[team]

    fun addLives(playerName: String, amount: Int)
    {
        when (livesMode)
        {
            Mode.PLAYER -> playerLives[playerName] = playerLives.getValue(playerName) + amount
            Mode.TEAM -> teamLives[PlayerManager.getTeam(playerName)] = teamLives.getValue(PlayerManager.getTeam(playerName)) + amount
        }
        DisplayManager.updateLivesDisplay()
    }

    fun getNonZeroLifePlayers() = playerLives.filter { it.value > 0 }

    fun getNonZeroLifeTeams() = teamLives.filter { it.value > 0 }

    fun reset()
    {
        playerLives.clear()
        teamLives.clear()
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