package de.jagenka.config

import de.jagenka.Coordinates
import de.jagenka.managers.Platform
import kotlinx.serialization.Serializable

@Serializable
class ConfigEntry(
    val spawns: SpawnsConfigEntry,
    val bonus: BonusPlatformsConfigEntry,
    val lives: LivesConfigEntry,
    val money: MoneyConfigEntry
)
{
    companion object
    {
        val dummy = ConfigEntry(SpawnsConfigEntry.dummy, BonusPlatformsConfigEntry.dummy, LivesConfigEntry.dummy, MoneyConfigEntry.dummy)
    }
}

@Serializable
class SpawnsConfigEntry(
    val spawnPositions: List<Coordinates>,
    val platformRadius: Int,
    val worldSpawn: Coordinates,
    val spectatorSpawn: Coordinates,
    val shuffleInterval: Int,
    val shuffleDelayAfterKill: Int
)
{
    companion object
    {
        val dummy = SpawnsConfigEntry(
            listOf(Coordinates(0.0, 0.0, 0.0, 1f, 1f), Coordinates(0.0, 0.0, 0.0, 1f, 1f)),
            0,
            Coordinates(0.0, 0.0, 0.0, 1f, 1f),
            Coordinates(0.0, 0.0, 0.0, 1f, 1f),
            0,
            0
        )
    }
}

@Serializable
class BonusPlatformsConfigEntry(
    val platforms: List<Platform>,
    val radius: Int,
    val spawnInterval: Int,
    val stayTime: Int,
    val initialSpawn: Int,
    val moneyAmount: Int,
    val moneyInterval: Int
)
{
    companion object
    {
        val dummy = BonusPlatformsConfigEntry(listOf(Platform("bonus1", Coordinates(0, 0, 0)), Platform("bonus2", Coordinates(0, 0, 0))), 0, 0, 0, 0, 0, 0)
    }
}

@Serializable
class LivesConfigEntry(val perPlayer: Int, val perTeam: Int)
{
    companion object
    {
        val dummy = LivesConfigEntry(0, 0)
    }
}

@Serializable
class MoneyConfigEntry(val start: Int, val amountPerInterval: Int, val interval: Int, val perKill: Int, val perKillStreakKill: Int)
{
    companion object
    {
        val dummy = MoneyConfigEntry(0, 0, 0, 0, 0)
    }
}