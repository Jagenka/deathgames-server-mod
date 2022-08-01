package de.jagenka.stats.tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Stats : Table()
{
    val playerName: Column<String> = varchar("playerName", 30)
    val gameId: Column<Long> = long("gameId").references(Games.gameStart)
    val team: Column<String> = varchar("team", 12)
    val damageDealt: Column<Float> = float("damageDealt")
    val damageTaken: Column<Float> = float("damageTaken")
    val healthRegenerated: Column<Float> = float("healthRegenerated")
    val highestKillStreak: Column<Int> = integer("highestKillStreak")
    val moneyEarned: Column<Int> = integer("moneyEarned")
    val moneySpent: Column<Int> = integer("moneySpent")
    val accountBalanceAverage: Column<Float> = float("accountBalanceAverage")
    val cmMovedOnGround: Column<Long> = long("cmMovedOnGround")
    val cmMovedInWater: Column<Long> = long("cmMovedInWater")
    val cmFallen: Column<Long> = long("cmFallen")
    val cmFlown: Column<Long> = long("cmFlown")
    val cmByElytra: Column<Long> = long("cmByElytra")
    val cmClimbed: Column<Long> = long("cmClimbed")
    val timesJumped: Column<Int> = integer("timesJumped")
    val ticksOnBonus: Column<Int> = integer("ticksOnBonus")
    val spawnsCaptured: Column<Int> = integer("spawnsCaptured")
    val timesCaughtInTrap: Column<Int> = integer("timesCaughtInTrap")
}