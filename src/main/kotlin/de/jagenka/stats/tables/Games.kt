package de.jagenka.stats.tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Games : Table()
{
    val gameStart: Column<Long> = long("gameStart")
    val gameEnd: Column<Long> = long("gameEnd")
    val mapName: Column<String> = varchar("mapName", 50)
    val winner: Column<String> = varchar("winner", 12) // String representation of `DGTeam?`
    val options: Column<String> = text("options")

    override val primaryKey = PrimaryKey(gameStart)
}