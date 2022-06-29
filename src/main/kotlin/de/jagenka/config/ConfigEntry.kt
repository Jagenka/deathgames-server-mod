package de.jagenka.config

import de.jagenka.BlockCuboid
import de.jagenka.CoordinateList
import de.jagenka.Coordinates
import de.jagenka.managers.Platform
import de.jagenka.timer.seconds
import kotlinx.serialization.Serializable

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Section(val name: String)

@Serializable
class ConfigEntry(
    @Section("general") val general: GeneralConfigEntry = GeneralConfigEntry(),
    @Section("spawns") val spawns: SpawnsConfigEntry = SpawnsConfigEntry(),
    @Section("bonus") val bonus: BonusPlatformsConfigEntry = BonusPlatformsConfigEntry(),
    @Section("respawns") val respawns: RespawnsConfigEntry = RespawnsConfigEntry(),
    @Section("money") val money: MoneyConfigEntry = MoneyConfigEntry(),
    @Section("shop") val shopSettings: ShopSettingsConfigEntry = ShopSettingsConfigEntry(),
    @Section("misc") val misc: MiscConfigEntry = MiscConfigEntry(),
    @Section("traps") val traps: TrapConfigEntry = TrapConfigEntry()
)

@Serializable
class GeneralConfigEntry(
    var enabled: Boolean = false
)

@Serializable
class SpawnsConfigEntry(
    var spawnPositions: CoordinateList = CoordinateList(listOf(Coordinates(0, 0, 0, 1f, 1f), Coordinates(0, 0, 0, 1f, 1f))),
    var platformRadius: Int = 0,
    var spectatorSpawn: Coordinates = Coordinates(0, 0, 0, 1f, 1f),
    var lobbySpawn: Coordinates = Coordinates(0, 0, 0, 1f, 1f),
    var shuffleInterval: Int = 0,
    var shuffleDelayAfterKill: Int = 0,
    var captureTimeNeeded: Int = 0,
    var captureEnabled: Boolean = false
)

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

@Serializable
class RespawnsConfigEntry(
    var perPlayer: Int = 0,
    var perTeam: Int = 0
)

@Serializable
class MoneyConfigEntry(
    var start: Int = 0,
    var amountPerInterval: Int = 0,
    var interval: Int = 0,
    var perKill: Int = 0,
    var perKillStreakKill: Int = 0
)

@Serializable
class ShopSettingsConfigEntry(
    var shopBounds: BlockCuboid = BlockCuboid(Coordinates(0, 0, 0), Coordinates(0, 0, 0)),
    var tpOutOfShopAfter: Int = 0,
    var refundPercent: Int = 0
)

@Serializable
class MiscConfigEntry(
    var revealTimePerPlayer: Int = 0,
    var shopCloseTimeAfterReveal: Int = 0,
    var killStreakPenaltyCap: Int = 0,
    var arenaBounds: BlockCuboid = BlockCuboid(Coordinates(0, 0, 0), Coordinates(0, 0, 0)),
    var spectatorRadiusPadding: Int = 0,
    var lobbyBounds: BlockCuboid = BlockCuboid(Coordinates(0, 0, 0), Coordinates(0, 0, 0))
)

@Serializable
class TrapConfigEntry(
    var triggerRange: Double = 0.5,
    var setupTime: Int = 10.seconds(),
    var triggerVisibilityRange: Double = 30.0,
    var visibilityRange: Double = 10.0,
    var affectedRange: Double = 1.5,
    var triggerDuration: Int = 6.seconds()
)