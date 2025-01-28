package de.jagenka.stats.tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Options : Table()
{
    val gameStart: Column<Long> = long("game_start")
    val optionID: Column<String> = varchar("option_id", 64)
    val optionValue: Column<String> = varchar("option_value", 64)
}