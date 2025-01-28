package de.jagenka.stats

import de.jagenka.team.DGTeam

class PlayerEntry(
    var games: MutableSet<PersonalGameEntry> = mutableSetOf()
)

class GameEntry(
    var gameId: Long = 0,
    var gameEnd: Long = 0,
    var map: String = "",
    var winner: DGTeam? = null,
    var options: MutableMap<String, String> = mutableMapOf(),
)
{
    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PersonalGameEntry

        if (gameId != other.gameId) return false

        return true
    }

    override fun hashCode(): Int
    {
        return gameId.hashCode()
    }
}

class PersonalGameEntry(
    var gameId: Long = 0,
    var team: DGTeam? = null,
    var kills: MutableList<KillEntry> = mutableListOf(),
    var deaths: MutableList<DeathEntry> = mutableListOf(),
    var damageDealt: Float = 0f,
    var damageTaken: Float = 0f,
    var healthRegenerated: Float = 0f,
    var highestKillStreak: Int = 0,
    var moneyEarned: Int = 0,
    var moneySpent: Int = 0,
    var accountBalanceAverage: Float = 0f,
    var cmMovedOnGround: Long = 0,
    var cmMovedInWater: Long = 0,
    var cmFallen: Long = 0,
    var cmFlown: Long = 0,
    var cmByElytra: Long = 0,
    var cmClimbed: Long = 0,
    var timesJumped: Int = 0,
    var ticksOnBonus: Int = 0,
    var spawnsCaptured: Int = 0,
    var timesCaughtInTrap: Int = 0,
    var itemsBought: List<ItemBoughtEntry> = listOf(),
)
{
    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PersonalGameEntry

        if (gameId != other.gameId) return false

        return true
    }

    override fun hashCode(): Int
    {
        return gameId.hashCode()
    }
}

class KillEntry(
    var deceased: String,
    var damageType: String,
    var time: Long
)

class DeathEntry(
    var damageType: String,
    var time: Long
)

class ItemBoughtEntry(
    var name: String,
    var price: Int,
    var time: Long
)

class StatsBaseEntry(
    var playedGames: MutableSet<GameEntry> = mutableSetOf(),
    var playerEntries: MutableMap<String, PlayerEntry> = mutableMapOf()
)
