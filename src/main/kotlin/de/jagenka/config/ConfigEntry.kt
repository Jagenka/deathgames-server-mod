package de.jagenka.config

import de.jagenka.BlockCuboid
import de.jagenka.CoordinateList
import de.jagenka.Coordinates
import de.jagenka.managers.Platform
import kotlinx.serialization.Serializable

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Section(val name: String)

@Serializable
class ConfigEntry(
    @Section("spawns") val spawns: SpawnsConfigEntry = SpawnsConfigEntry(),
    @Section("bonus") val bonus: BonusPlatformsConfigEntry = BonusPlatformsConfigEntry(),
    @Section("lives") val lives: LivesConfigEntry = LivesConfigEntry(),
    @Section("money") val money: MoneyConfigEntry = MoneyConfigEntry(),
    @Section("shop") val shopSettings: ShopSettingsConfigEntry = ShopSettingsConfigEntry(),
    @Section("misc") val misc: MiscConfigEntry = MiscConfigEntry()
)
{
    companion object
    {
        val dummy = ConfigEntry()
    }
}

@Serializable
class SpawnsConfigEntry(
    var spawnPositions: CoordinateList = CoordinateList(listOf(Coordinates(0, 0, 0, 1f, 1f), Coordinates(0, 0, 0, 1f, 1f))),
    var platformRadius: Int = 0,
    var worldSpawn: Coordinates = Coordinates(0, 0, 0, 1f, 1f),
    var spectatorSpawn: Coordinates = Coordinates(0, 0, 0, 1f, 1f),
    var lobbySpawn: Coordinates = Coordinates(0, 0, 0, 1f, 1f),
    var shuffleInterval: Int = 0,
    var shuffleDelayAfterKill: Int = 0
)
{
    companion object
    {
        val dummy = SpawnsConfigEntry()
    }
}

@Serializable
class BonusPlatformsConfigEntry(
    var platforms: List<Platform> = listOf(Platform("bonus1", Coordinates(0, 0, 0)), Platform("bonus2", Coordinates(0, 0, 0))),
    var radius: Int = 0,
    var spawnInterval: Int = 0,
    var stayTime: Int = 0,
    var initialSpawn: Int = 0,
    var moneyAmount: Int = 0,
    var moneyInterval: Int = 0
)
{
    companion object
    {
        val dummy = BonusPlatformsConfigEntry()
    }
}

@Serializable
class LivesConfigEntry(
    var perPlayer: Int = 0,
    var perTeam: Int = 0
)
{
    companion object
    {
        val dummy = LivesConfigEntry()
    }
}

@Serializable
class MoneyConfigEntry(
    var start: Int = 0,
    var amountPerInterval: Int = 0,
    var interval: Int = 0,
    var perKill: Int = 0,
    var perKillStreakKill: Int = 0
)
{
    companion object
    {
        val dummy = MoneyConfigEntry()
    }
}

@Serializable
class ShopSettingsConfigEntry(
    var shopBounds: BlockCuboid = BlockCuboid(Coordinates(0, 0, 0), Coordinates(0, 0, 0)),
    var tpOutOfShopAfter: Int = 0,
    var refundPercent: Int = 0
)
{
    companion object
    {
        val dummy = ShopSettingsConfigEntry()
    }
}

@Serializable
class MiscConfigEntry(
    var revealTimePerPlayer: Int = 0,
    var arenaBounds: BlockCuboid = BlockCuboid(Coordinates(0, 0, 0), Coordinates(0, 0, 0)),
    var spectatorRadiusPadding: Int = 0
)
{
    companion object
    {
        val dummy = MiscConfigEntry()
    }
}