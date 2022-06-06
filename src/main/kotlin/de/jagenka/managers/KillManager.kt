package de.jagenka.managers

import de.jagenka.DGTeam
import de.jagenka.Util.sendChatMessage
import de.jagenka.Util.sendPrivateMessage
import de.jagenka.config.Config.livesPerPlayer
import de.jagenka.config.Config.livesPerTeam
import de.jagenka.config.Config.moneyBonusPerKillStreakKill
import de.jagenka.config.Config.moneyPerKill
import de.jagenka.config.Config.startMoneyPerPlayer
import de.jagenka.managers.MoneyManager.addMoney
import de.jagenka.managers.MoneyManager.setMoney
import de.jagenka.managers.PlayerManager.eliminate
import de.jagenka.managers.PlayerManager.getDGTeam
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

    var moneyMode = Mode.PLAYER
    var livesMode = Mode.TEAM
    var killStreakMode = Mode.PLAYER


    @JvmStatic
    fun handleDeath(attacker: Entity?, deceased: ServerPlayerEntity)
    {
        if (attacker is ServerPlayerEntity)
        {
            sendChatMessage("${attacker.name.asString()} killed ${deceased.name.asString()}")
            totalKills[attacker.name.asString()] = totalKills.getValue(attacker.name.asString()) + 1
            when (killStreakMode)
            {
                Mode.PLAYER -> playerKillStreak[attacker.name.asString()] = playerKillStreak.getValue(attacker.name.asString()) + 1
                Mode.TEAM -> teamKillStreak[attacker.getDGTeam()] = teamKillStreak.getValue(attacker.getDGTeam()) + 1
            }
            handleMoney(attacker, deceased)
        }

        totalDeaths[deceased.name.asString()] = totalDeaths.getValue(deceased.name.asString()) + 1
        handleLives(deceased)
        resetKillStreak(deceased)

        DisplayManager.updateLivesDisplay()

        // TODO?: reset shop teleport after kill
        ShuffleSpawnsTask.updateLastKillTime()
    }

    private fun handleMoney(attacker: ServerPlayerEntity, deceased: ServerPlayerEntity)
    {
        when (moneyMode)
        {
            Mode.PLAYER ->
            {
                val killStreakAmount = getKillStreak(deceased)
                addMoney(attacker.name.asString(), moneyPerKill + moneyBonusPerKillStreakKill * killStreakAmount)
                sendChatMessage("They made $killStreakAmount kill${if (killStreakAmount != 1) "s" else ""} since their previous death.")
                attacker.sendPrivateMessage("You received ${moneyPerKill + moneyBonusPerKillStreakKill * killStreakAmount}")
            }
            Mode.TEAM ->
            {
                val killStreakAmount = getKillStreak(deceased)
                addMoney(attacker.getDGTeam(), moneyPerKill + moneyBonusPerKillStreakKill * killStreakAmount)
                sendChatMessage("${attacker.getDGTeam()?.name ?: "They"} made $killStreakAmount kill${if (killStreakAmount != 1) "s" else ""} since their previous death.")
                attacker.getDGTeam()?.getOnlinePlayers()?.forEach { it.sendPrivateMessage("Your team received ${moneyPerKill + moneyBonusPerKillStreakKill * killStreakAmount}") }
            }
        }

        DisplayManager.updateLevelDisplay()
    }

    private fun getKillStreak(deceased: ServerPlayerEntity): Int
    {
        return when (killStreakMode)
        {
            Mode.PLAYER -> playerKillStreak.getValue(deceased.name.asString())
            Mode.TEAM -> teamKillStreak.getValue(deceased.getDGTeam())
        }
    }

    private fun resetKillStreak(deceased: ServerPlayerEntity)
    {
        when (killStreakMode)
        {
            Mode.PLAYER -> playerKillStreak[deceased.name.asString()] = 0
            Mode.TEAM -> teamKillStreak[deceased.getDGTeam()] = 0
        }
    }

    private fun handleLives(deceased: ServerPlayerEntity)
    {
        when (livesMode)
        {
            Mode.PLAYER ->
            {
                val livesAmount = playerLives.getValue(deceased.name.asString())
                if (livesAmount > 0) playerLives[deceased.name.asString()] = livesAmount - 1
                if (livesAmount - 1 < 1) deceased.eliminate()
            }
            Mode.TEAM ->
            {
                val livesAmount = teamLives.getValue(deceased.getDGTeam())
                if (livesAmount > 0) teamLives[deceased.getDGTeam()] = livesAmount - 1
                if (livesAmount - 1 < 1) deceased.eliminate()
            }
        }
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

    fun getPlayerLives() = playerLives
    fun getTeamLives() = teamLives

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