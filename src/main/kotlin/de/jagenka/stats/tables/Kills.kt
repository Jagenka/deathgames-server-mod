package de.jagenka.stats.tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Kills : Table()
{
    val gameId: Column<Long> = long("gameId").references(Stats.gameId)
    val playerName: Column<String> = varchar("playerName", 30).references(Stats.playerName)
    val deceased: Column<String> = varchar("deceased", 30)
    val damageType: Column<String> = varchar("damageType", 20) // String representation of DamageType
    val time: Column<Long> = long("time")
}