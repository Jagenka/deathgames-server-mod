package de.jagenka.stats.tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object ItemsBought : Table()
{
    val gameId: Column<Long> = long("gameId").references(Stats.gameId)
    val playerName: Column<String> = varchar("playerName", 30).references(Stats.playerName)
    val itemName: Column<String> = varchar("itemName", 50)
    val price: Column<Int> = integer("price")
    val time: Column<Long> = long("time")
}