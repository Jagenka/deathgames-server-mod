package de.jagenka

import de.jagenka.Config.moneyBonusPerKillStreakKill
import de.jagenka.Config.livesPerPlayer
import de.jagenka.Config.livesPerTeam
import de.jagenka.Config.moneyPerKill
import de.jagenka.Config.startMoneyPerPlayer
import de.jagenka.DGPlayerManager.eliminate
import de.jagenka.DGPlayerManager.getDGTeam
import de.jagenka.Util.sendChatMessage
import de.jagenka.Util.sendPrivateMessage
import de.jagenka.timer.ShuffleSpawnsTask
import net.minecraft.entity.Entity
import net.minecraft.server.network.ServerPlayerEntity

object DGKillManager
{
    private val playerMoney = mutableMapOf<ServerPlayerEntity, Int>().withDefault { 0 }
    private val teamMoney = mutableMapOf<DGTeam?, Int>().withDefault { 0 }

    private val playerLives = mutableMapOf<ServerPlayerEntity, Int>().withDefault { 0 }
    private val teamLives = mutableMapOf<DGTeam?, Int>().withDefault { 0 }

    private val playerKillStreak = mutableMapOf<ServerPlayerEntity, Int>().withDefault { 0 }
    private val teamKillStreak = mutableMapOf<DGTeam?, Int>().withDefault { 0 }

    private val totalKills = mutableMapOf<ServerPlayerEntity, Int>().withDefault { 0 }
    private val totalDeaths = mutableMapOf<ServerPlayerEntity, Int>().withDefault { 0 }

    var moneyMode = Mode.PLAYER
    var livesMode = Mode.TEAM
    var killStreakMode = Mode.PLAYER


    @JvmStatic
    fun handleDeath(attacker: Entity?, deceased: ServerPlayerEntity)
    {
        if (attacker is ServerPlayerEntity) //TODO: seems to not work
        {
            sendChatMessage("${attacker.name.asString()} killed ${deceased.name.asString()}")
            totalKills[attacker] = totalKills.getValue(attacker) + 1
            when (killStreakMode)
            {
                Mode.PLAYER -> playerKillStreak[attacker] = playerKillStreak.getValue(attacker) + 1
                Mode.TEAM -> teamKillStreak[attacker.getDGTeam()] = teamKillStreak.getValue(attacker.getDGTeam()) + 1
            }
            handleMoney(attacker, deceased)
        }

        totalDeaths[deceased] = totalDeaths.getValue(deceased) + 1
        handleLives(deceased)
        resetKillStreak(deceased)

        DGDisplayManager.updateSidebar()

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
                addMoney(attacker, moneyPerKill + moneyBonusPerKillStreakKill * killStreakAmount)
                sendChatMessage("They made $killStreakAmount kill${if (killStreakAmount != 1) "s" else ""} since their previous death.")
                attacker.sendPrivateMessage("You received ${moneyPerKill + moneyBonusPerKillStreakKill * killStreakAmount}")
            }
            Mode.TEAM ->
            {
                val killStreakAmount = getKillStreak(deceased)
                addMoney(attacker.getDGTeam(), moneyPerKill + moneyBonusPerKillStreakKill * killStreakAmount)
                sendChatMessage("${attacker.getDGTeam()?.name ?: "They"} made $killStreakAmount kill${if (killStreakAmount != 1) "s" else ""} since their previous death.")
                attacker.getDGTeam()?.getPlayers()?.forEach { it.sendPrivateMessage("Your team received ${moneyPerKill + moneyBonusPerKillStreakKill * killStreakAmount}") }
            }
        }

        DGDisplayManager.updateLevelDisplay()
    }

    private fun getKillStreak(deceased: ServerPlayerEntity): Int
    {
        return when (killStreakMode)
        {
            Mode.PLAYER -> playerKillStreak.getValue(deceased)
            Mode.TEAM -> teamKillStreak.getValue(deceased.getDGTeam())
        }
    }

    private fun resetKillStreak(deceased: ServerPlayerEntity)
    {
        when (killStreakMode)
        {
            Mode.PLAYER -> playerKillStreak[deceased] = 0
            Mode.TEAM -> teamKillStreak[deceased.getDGTeam()] = 0
        }
    }

    private fun handleLives(deceased: ServerPlayerEntity)
    {
        when (livesMode)
        {
            Mode.PLAYER ->
            {
                val livesAmount = playerLives.getValue(deceased)
                if (livesAmount > 0) playerLives[deceased] = livesAmount - 1
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

    fun initLives(players: Collection<ServerPlayerEntity>)
    {
        when (livesMode)
        {
            Mode.PLAYER -> players.forEach { playerLives[it] = livesPerPlayer }
            Mode.TEAM -> players.forEach { teamLives[it.getDGTeam()] = livesPerTeam }
        }
    }

    fun initMoney(players: Collection<ServerPlayerEntity>)
    {
        when (moneyMode)
        {
            Mode.PLAYER -> players.forEach { setMoney(it, startMoneyPerPlayer) }
            Mode.TEAM ->
            {
                DGPlayerManager.getInGameTeams().forEach { inGameTeam ->
                    setMoney(inGameTeam, players.filter { player -> player.getDGTeam() == inGameTeam }.size * startMoneyPerPlayer)
                }
            }
        }
    }

    fun getPlayerLives() = playerLives
    fun getTeamLives() = teamLives

    fun getMoney(player: ServerPlayerEntity) = playerMoney.getValue(player)
    fun getMoney(team: DGTeam?) = teamMoney.getValue(team)

    fun setMoney(player: ServerPlayerEntity, amount: Int)
    {
        playerMoney[player] = amount
        DGDisplayManager.updateLevelDisplay()
    }

    fun setMoney(team: DGTeam?, amount: Int)
    {
        if (team == null) return
        teamMoney[team] = amount
        DGDisplayManager.updateLevelDisplay()
    }

    fun addMoney(player: ServerPlayerEntity, amount: Int)
    {
        setMoney(player, getMoney(player) + amount)
    }

    fun addMoney(team: DGTeam?, amount: Int)
    {
        if (team == null) return
        setMoney(team, getMoney(team) + amount)
    }

    fun getLives(player: ServerPlayerEntity) = playerLives[player]
    fun getLives(team: DGTeam) = teamLives[team]

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
    return when (DGKillManager.moneyMode)
    {
        Mode.PLAYER -> DGKillManager.getMoney(this)
        Mode.TEAM -> this.getDGTeam()?.let { DGKillManager.getMoney(it) } ?: 0
    }
}

fun ServerPlayerEntity.addDGMoney(amount: Int)
{
    if (DGKillManager.moneyMode != Mode.PLAYER) return
    DGKillManager.addMoney(this, amount)
}