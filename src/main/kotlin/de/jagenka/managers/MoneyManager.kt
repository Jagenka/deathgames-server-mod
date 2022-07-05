package de.jagenka.managers

import de.jagenka.config.Config
import de.jagenka.managers.DisplayManager.sendPrivateMessage
import de.jagenka.managers.MoneyManager.getMoney
import de.jagenka.managers.MoneyManager.moneyMode
import de.jagenka.managers.MoneyManager.refundMoney
import de.jagenka.managers.PlayerManager.getDGTeam
import de.jagenka.shop.Shop
import de.jagenka.stats.StatManager
import de.jagenka.stats.gib
import de.jagenka.team.DGTeam
import net.minecraft.server.network.ServerPlayerEntity

object MoneyManager
{
    var moneyMode = Mode.PLAYER

    private val playerMoney = mutableMapOf<String, Int>().withDefault { 0 }
    private val teamMoney = mutableMapOf<DGTeam?, Int>().withDefault { 0 }

    fun getMoney(playerName: String) = playerMoney.getValue(playerName)
    fun getMoney(player: ServerPlayerEntity) = getMoney(player.name.string)
    fun getMoney(team: DGTeam?) = teamMoney.getValue(team)

    fun initMoney()
    {
        val players = PlayerManager.getPlayers()

        when (moneyMode)
        {
            Mode.PLAYER -> players.forEach {
                setMoney(it, Config.startMoneyPerPlayer)
                StatManager.personalStats.gib(it).moneyEarned += Config.startMoneyPerPlayer
            }
            Mode.TEAM ->
            {
                PlayerManager.getParticipatingTeams().forEach { participatingTeam ->
                    val teamSize = participatingTeam.getPlayers().size
                    setMoney(participatingTeam, teamSize * Config.startMoneyPerPlayer)
                    participatingTeam.getPlayers().forEach { StatManager.personalStats.gib(it).moneyEarned += teamSize * Config.startMoneyPerPlayer }
                }
            }
        }
    }

    private fun setMoney(playerName: String, amount: Int)
    {
        playerMoney[playerName] = amount
        DisplayManager.updateLevelDisplay()
    }

    private fun setMoney(team: DGTeam?, amount: Int)
    {
        if (team == null) return
        teamMoney[team] = amount
        DisplayManager.updateLevelDisplay()
    }

    fun addMoney(playerName: String, amount: Int)
    {
        setMoney(playerName, getMoney(playerName) + amount)
        if (amount >= 0)
        {
            StatManager.personalStats.gib(playerName).moneyEarned += amount
        }
    }

    fun addMoney(team: DGTeam?, amount: Int)
    {
        if (team == null) return
        setMoney(team, getMoney(team) + amount)
        if (amount >= 0)
        {
            team.getPlayers().forEach { playerName ->
                StatManager.personalStats.gib(playerName).moneyEarned += amount
            }
        }
    }

    fun refundMoney(playerName: String, amount: Int)
    {
        setMoney(playerName, getMoney(playerName) + amount)
    }

    fun refundMoney(team: DGTeam?, amount: Int)
    {
        if (team == null) return
        setMoney(team, getMoney(team) + amount)
    }

    fun handleMoneyOnPlayerKill(attacker: ServerPlayerEntity, deceased: ServerPlayerEntity)
    {
        when (moneyMode)
        {
            Mode.PLAYER ->
            {
                val killStreakAmount = KillManager.getKillStreak(deceased.name.string)
                addMoney(attacker.name.string, Config.moneyPerKill + Config.moneyBonusPerKillStreakKill * killStreakAmount)
//                sendChatMessage("They made $killStreakAmount kill${if (killStreakAmount != 1) "s" else ""} since their previous death.")
                attacker.sendPrivateMessage("You receive ${Shop.SHOP_UNIT}${Config.moneyPerKill + Config.moneyBonusPerKillStreakKill * killStreakAmount}.")
            }
            Mode.TEAM ->
            {
                val killStreakAmount = KillManager.getKillStreak(deceased.name.string)
                addMoney(attacker.getDGTeam(), Config.moneyPerKill + Config.moneyBonusPerKillStreakKill * killStreakAmount)
//                sendChatMessage("${attacker.getDGTeam()?.name ?: "They"} made $killStreakAmount kill${if (killStreakAmount != 1) "s" else ""} since their previous death.")
                attacker.getDGTeam()?.getOnlinePlayers()
                    ?.forEach { it.sendPrivateMessage("Your team receives ${Shop.SHOP_UNIT}${Config.moneyPerKill + Config.moneyBonusPerKillStreakKill * killStreakAmount}.") }
            }
        }

        DisplayManager.updateLevelDisplay()
    }

    fun reset()
    {
        playerMoney.clear()
        teamMoney.clear()
    }
}

fun ServerPlayerEntity.getDGMoney(): Int
{
    return when (moneyMode)
    {
        Mode.PLAYER -> getMoney(this)
        Mode.TEAM -> this.getDGTeam()?.let { getMoney(it) } ?: 0
    }
}

fun ServerPlayerEntity.deductDGMoney(amount: Int)
{
    StatManager.personalStats.gib(this.name.string).moneySpent += amount
    when (moneyMode)
    {
        Mode.PLAYER -> refundMoney(this.name.string, -amount)
        Mode.TEAM -> refundMoney(this.getDGTeam(), -amount)
    }
}