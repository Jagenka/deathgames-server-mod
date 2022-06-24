package de.jagenka.stats

import de.jagenka.team.DGTeam
import kotlinx.serialization.Serializable

@Serializable
class PlayerEntry(
    var games: MutableSet<PersonalGameEntry> = mutableSetOf()
)

@Serializable
class GameEntry(
    var gameId: Long = 0,
    var gameEnd: Long = 0,
    var captureEnabled: Boolean = false,
    var winner: DGTeam? = null,
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

@Serializable
class PersonalGameEntry(
    var gameId: Long = 0,
    var team: DGTeam? = null,
    var kills: MutableList<KillEntry> = mutableListOf(),
    var deaths: MutableList<DeathEntry> = mutableListOf(),
    var damageDealt: Float = 0f,
    var damageTaken: Float = 0f,
    var highestKillStreak: Int = 0,
    var moneyEarned: Int = 0,
    var moneySpent: Int = 0,
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
    var itemsBought: List<String> = listOf(),
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

@Serializable
class KillEntry(
    var deceased: String,
    var damageType: DamageType,
    var time: Long
)

@Serializable
class DeathEntry(
    var damageType: DamageType,
    var time: Long
)

@Serializable
class StatsBaseEntry(
    var playedGames: MutableSet<GameEntry> = mutableSetOf(),
    var playerEntries: MutableMap<String, PlayerEntry> = mutableMapOf()
)
