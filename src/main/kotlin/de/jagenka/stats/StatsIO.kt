package de.jagenka.stats

import de.jagenka.stats.tables.*
import net.fabricmc.loader.api.FabricLoader
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Path

object StatsIO
{
    fun loadStats()
    {
        val pathToStatsFile: Path = FabricLoader.getInstance().configDir.resolve("deathgames_stats.sqlite")

        Database.connect("jdbc:sqlite:$pathToStatsFile")

        transaction {
            SchemaUtils.create(Deaths, Games, ItemsBought, Kills, Stats)
        }
    }

    fun storeGame(gameEntry: GameEntry)
    {
        transaction {
            Games.insert {
                it[gameStart] = gameEntry.gameId
                it[gameEnd] = gameEntry.gameEnd
                it[mapName] = gameEntry.map
                it[winner] = gameEntry.winner?.name ?: "null"
                it[options] = gameEntry.options
            }
        }
    }

    fun storePlayer(name: String, personalGameEntry: PersonalGameEntry)
    {
        transaction {
            Stats.insert {
                it[playerName] = name
                it[gameId] = personalGameEntry.gameId
                it[team] = personalGameEntry.team?.name ?: "null"
                it[damageDealt] = personalGameEntry.damageDealt
                it[damageTaken] = personalGameEntry.damageTaken
                it[healthRegenerated] = personalGameEntry.healthRegenerated
                it[highestKillStreak] = personalGameEntry.highestKillStreak
                it[moneyEarned] = personalGameEntry.moneyEarned
                it[moneySpent] = personalGameEntry.moneySpent
                it[accountBalanceAverage] = personalGameEntry.accountBalanceAverage
                it[cmMovedOnGround] = personalGameEntry.cmMovedOnGround
                it[cmMovedInWater] = personalGameEntry.cmMovedInWater
                it[cmFallen] = personalGameEntry.cmFallen
                it[cmFlown] = personalGameEntry.cmFlown
                it[cmByElytra] = personalGameEntry.cmByElytra
                it[cmClimbed] = personalGameEntry.cmClimbed
                it[timesJumped] = personalGameEntry.timesJumped
                it[ticksOnBonus] = personalGameEntry.ticksOnBonus
                it[spawnsCaptured] = personalGameEntry.spawnsCaptured
                it[timesCaughtInTrap] = personalGameEntry.timesCaughtInTrap
            }

            personalGameEntry.kills.forEach { killEntry ->
                Kills.insert {
                    it[gameId] = personalGameEntry.gameId
                    it[Stats.playerName] = name
                    it[deceased] = killEntry.deceased
                    it[damageType] = killEntry.damageType.name
                    it[time] = killEntry.time
                }
            }

            personalGameEntry.deaths.forEach { deathEntry ->
                Deaths.insert {
                    it[gameId] = personalGameEntry.gameId
                    it[playerName] = name
                    it[damageType] = deathEntry.damageType.name
                    it[time] = deathEntry.time
                }
            }

            personalGameEntry.itemsBought.forEach { itemBought ->
                ItemsBought.insert {
                    it[gameId] = personalGameEntry.gameId
                    it[playerName] = name
                    it[itemName] = itemBought.name
                    it[price] = itemBought.price
                    it[time] = itemBought.time
                }
            }
        }
    }
}
