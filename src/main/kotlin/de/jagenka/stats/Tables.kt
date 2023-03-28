package de.jagenka.stats

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table

fun Table.sqliteLong(name: String): Column<Long> = registerColumn(name, SQLiteLongType())
fun Table.sqliteInt(name: String): Column<Int> = registerColumn(name, SQLiteIntType())
fun Table.sqliteDouble(name: String): Column<Double> = registerColumn(name, SQLiteDoubleType())

class SQLiteLongType : ColumnType()
{
    override fun sqlType(): String = "INTEGER"
    override fun valueFromDB(value: Any): Long = when (value)
    {
        is Long -> value
        is Number -> value.toLong()
        is String -> value.toLong()
        else -> error("Unexpected value of type SQLiteLong: $value of ${value::class.qualifiedName}")
    }
}

class SQLiteIntType : ColumnType()
{
    override fun sqlType(): String = "INTEGER"
    override fun valueFromDB(value: Any): Int = when (value)
    {
        is Int -> value
        is Number -> value.toInt()
        is String -> value.toInt()
        else -> error("Unexpected value of type SQLiteInt: $value of ${value::class.qualifiedName}")
    }
}

class SQLiteDoubleType : ColumnType()
{
    override fun sqlType(): String = "DOUBLE"
    override fun valueFromDB(value: Any): Int = when (value)
    {
        is Int -> value
        is Number -> value.toInt()
        is String -> value.toInt()
        else -> error("Unexpected value of type SQLiteInt: $value of ${value::class.qualifiedName}")
    }
}

object Games : Table()
{
    val gameStart: Column<Long> = sqliteLong("gameStart")
    val gameEnd: Column<Long> = sqliteLong("gameEnd")
    val mapName: Column<String> = text("mapName")
    val winner: Column<String> = text("winner") // String representation of `DGTeam?`
    val options: Column<String> = text("options")

    override val primaryKey = PrimaryKey(gameStart)
}

object Kills : Table()
{
    val gameId: Column<Long> = sqliteLong("gameId")
    val playerName: Column<String> = text("playerName")
    val deceased: Column<String> = text("deceased")
    val damageType: Column<String> = text("damageType") // String representation of DamageType
    val time: Column<Long> = sqliteLong("time")
}

object Deaths : Table()
{
    val gameId: Column<Long> = sqliteLong("gameId")
    val playerName: Column<String> = text("playerName")
    val damageType: Column<String> = text("damageType") // String representation of DamageType
    val time: Column<Long> = sqliteLong("time")

    override val primaryKey: PrimaryKey = PrimaryKey(gameId, time, playerName)
}

object ItemsBought : Table()
{
    val id: Column<Int> = integer("id").autoIncrement() // used as primary key, as other infos may not suffice
    val gameId: Column<Long> = sqliteLong("gameId")
    val playerName: Column<String> = text("playerName")
    val itemName: Column<String> = text("itemName")
    val price: Column<Int> = sqliteInt("price")
    val time: Column<Long> = sqliteLong("time")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

object Stats : Table()
{
    val playerName: Column<String> = text("playerName")
    val gameId: Column<Long> = sqliteLong("gameId")
    val team: Column<String> = text("team")
    val damageDealt: Column<Double> = sqliteDouble("damageDealt")
    val damageTaken: Column<Double> = sqliteDouble("damageTaken")
    val healthRegenerated: Column<Double> = sqliteDouble("healthRegenerated")
    val highestKillStreak: Column<Int> = sqliteInt("highestKillStreak")
    val moneyEarned: Column<Int> = sqliteInt("moneyEarned")
    val moneySpent: Column<Int> = sqliteInt("moneySpent")
    val accountBalanceAverage: Column<Double> = sqliteDouble("accountBalanceAverage")
    val cmMovedOnGround: Column<Long> = sqliteLong("cmMovedOnGround")
    val cmMovedInWater: Column<Long> = sqliteLong("cmMovedInWater")
    val cmFallen: Column<Long> = sqliteLong("cmFallen")
    val cmFlown: Column<Long> = sqliteLong("cmFlown")
    val cmByElytra: Column<Long> = sqliteLong("cmByElytra")
    val cmClimbed: Column<Long> = sqliteLong("cmClimbed")
    val timesJumped: Column<Int> = sqliteInt("timesJumped")
    val ticksOnBonus: Column<Int> = sqliteInt("ticksOnBonus")
    val spawnsCaptured: Column<Int> = sqliteInt("spawnsCaptured")
    val timesCaughtInTrap: Column<Int> = sqliteInt("timesCaughtInTrap")

    override val primaryKey: PrimaryKey = PrimaryKey(gameId, playerName)
}