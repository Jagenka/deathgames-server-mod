package de.jagenka

import de.jagenka.Config.moneyBonusPerKillStreakKill
import de.jagenka.Config.livesPerPlayer
import de.jagenka.Config.livesPerTeam
import de.jagenka.Config.moneyPerKill
import de.jagenka.Config.startMoneyPerPlayer
import de.jagenka.DGKillManager.moneyMode
import de.jagenka.DGPlayerManager.eliminate
import de.jagenka.DGPlayerManager.getDGTeam
import de.jagenka.Util.sendChatMessage
import de.jagenka.Util.sendPrivateMessage
import de.jagenka.timer.ShuffleSpawnsTask
import net.minecraft.entity.Entity
import net.minecraft.server.network.ServerPlayerEntity

object DGKillManager
{
    private val playerMoney = mutableMapOf<String, Int>().withDefault { 0 }
    private val teamMoney = mutableMapOf<DGTeam?, Int>().withDefault { 0 }

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

        DGDisplayManager.updateLivesDisplay()

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

        DGDisplayManager.updateLevelDisplay()
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
        val players = DGPlayerManager.getPlayers()
        when (livesMode)
        {
            Mode.PLAYER -> players.forEach { playerLives[it] = livesPerPlayer }
            Mode.TEAM -> players.forEach { teamLives[DGPlayerManager.getTeam(it)] = livesPerTeam }
        }
    }

    fun initMoney()
    {
        val players = DGPlayerManager.getPlayers()

        when (moneyMode)
        {
            Mode.PLAYER -> players.forEach { setMoney(it, startMoneyPerPlayer) }
            Mode.TEAM ->
            {
                DGPlayerManager.getInGameTeams().forEach { inGameTeam ->
                    setMoney(inGameTeam, players.filter { playerName -> DGPlayerManager.getTeam(playerName) == inGameTeam }.size * startMoneyPerPlayer)
                }
            }
        }
    }

    fun getPlayerLives() = playerLives
    fun getTeamLives() = teamLives

    fun getMoney(playerName: String) = playerMoney.getValue(playerName)
    fun getMoney(player: ServerPlayerEntity) = getMoney(player.name.asString())
    fun getMoney(team: DGTeam?) = teamMoney.getValue(team)

    fun setMoney(playerName: String, amount: Int)
    {
        playerMoney[playerName] = amount
        DGDisplayManager.updateLevelDisplay()
    }

    fun setMoney(team: DGTeam?, amount: Int)
    {
        if (team == null) return
        teamMoney[team] = amount
        DGDisplayManager.updateLevelDisplay()
    }

    fun addMoney(playerName: String, amount: Int)
    {
        setMoney(playerName, getMoney(playerName) + amount)
    }

    fun addMoney(team: DGTeam?, amount: Int)
    {
        if (team == null) return
        setMoney(team, getMoney(team) + amount)
    }

    fun getLives(playerName: String) = playerLives[playerName]
    fun getLives(team: DGTeam) = teamLives[team]

    fun addLives(playerName: String, amount: Int)
    {
        when (livesMode)
        {
            Mode.PLAYER -> playerLives[playerName] = playerLives.getValue(playerName) + amount
            Mode.TEAM -> teamLives[DGPlayerManager.getTeam(playerName)] = teamLives.getValue(DGPlayerManager.getTeam(playerName)) + amount
        }
        DGDisplayManager.updateLivesDisplay()
    }

    fun getNonZeroLifePlayers() = playerLives.filter { it.value > 0 }

    fun getNonZeroLifeTeams() = teamLives.filter { it.value > 0 }

    fun reset()
    {
        playerMoney.clear()
        teamMoney.clear()
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

fun ServerPlayerEntity.getDGMoney(): Int
{
    return when (moneyMode)
    {
        Mode.PLAYER -> DGKillManager.getMoney(this)
        Mode.TEAM -> this.getDGTeam()?.let { DGKillManager.getMoney(it) } ?: 0
    }
}

fun ServerPlayerEntity.deductDGMoney(amount: Int)
{
    when (moneyMode)
    {
        Mode.PLAYER -> DGKillManager.addMoney(this.name.asString(), -amount)
        Mode.TEAM -> DGKillManager.addMoney(this.getDGTeam(), -amount)
    }
}