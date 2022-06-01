package de.jagenka.managers

import de.jagenka.DGTeam
import de.jagenka.managers.MoneyManager.addMoney
import de.jagenka.managers.MoneyManager.getMoney
import de.jagenka.managers.PlayerManager.getDGTeam
import net.minecraft.server.network.ServerPlayerEntity

object MoneyManager
{
    private val playerMoney = mutableMapOf<String, Int>().withDefault { 0 }
    private val teamMoney = mutableMapOf<DGTeam?, Int>().withDefault { 0 }

    fun getMoney(playerName: String) = playerMoney.getValue(playerName)
    fun getMoney(player: ServerPlayerEntity) = getMoney(player.name.asString())
    fun getMoney(team: DGTeam?) = teamMoney.getValue(team)

    fun setMoney(playerName: String, amount: Int)
    {
        playerMoney[playerName] = amount
        DisplayManager.updateLevelDisplay()
    }

    fun setMoney(team: DGTeam?, amount: Int)
    {
        if (team == null) return
        teamMoney[team] = amount
        DisplayManager.updateLevelDisplay()
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

    fun reset()
    {
        playerMoney.clear()
        teamMoney.clear()
    }
}

fun ServerPlayerEntity.getDGMoney(): Int
{
    return when (KillManager.moneyMode)
    {
        Mode.PLAYER -> getMoney(this)
        Mode.TEAM -> this.getDGTeam()?.let { getMoney(it) } ?: 0
    }
}

fun ServerPlayerEntity.deductDGMoney(amount: Int)
{
    when (KillManager.moneyMode)
    {
        Mode.PLAYER -> addMoney(this.name.asString(), -amount)
        Mode.TEAM -> addMoney(this.getDGTeam(), -amount)
    }
}