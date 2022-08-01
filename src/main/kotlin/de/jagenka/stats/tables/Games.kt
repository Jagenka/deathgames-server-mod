package de.jagenka.stats.tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Games : Table()
{
    val gameStart: Column<Long> = long("gameStart")
    val gameEnd: Column<Long> = long("gameEnd")
    val mapName: Column<String> = varchar("mapName", 50)
    val captureEnabled: Column<Boolean> = bool("captureEnabled")
    val winner: Column<String> = varchar("winner", 12) // String representation of DGTeam?

    override val primaryKey = PrimaryKey(gameStart)
}