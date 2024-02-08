package de.jagenka.config

import de.jagenka.*
import de.jagenka.managers.Platform
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
    @Section("displayedText") val displayedText: DisplayedTextConfigEntry = DisplayedTextConfigEntry(),
    @Section("shop") val shop: ShopConfig = ShopConfig()
)

@Serializable
class GeneralConfigEntry(
    var enabled: Boolean = false,
    var locale: String = "en",
    var arenaBounds: BlockCuboid = BlockCuboid(BlockPos(0, 0, 0), BlockPos(0, 0, 0)),
    var spectatorRadiusPadding: Int = 0,
    var lobbyBounds: BlockCuboid = BlockCuboid(BlockPos(0, 0, 0), BlockPos(0, 0, 0)),
)

// TODO: add optional default team to spawn positions (mainly for non-shuffling/capturing games, but also as initial assigment)

@Serializable
class SpawnsConfigEntry(
    var spawnPositions: CoordinateList = CoordinateList(listOf(Coordinates(0, 0, 0, 0f, 0f), Coordinates(0, 0, 0, 0f, 0f))),
    var platformRadius: Int = 0,
    var spectatorSpawn: Coordinates = Coordinates(0, 0, 0, 0f, 0f),
    var lobbySpawn: Coordinates = Coordinates(0, 0, 0, 0f, 0f),
    var enableShuffle: Boolean = false,
    var shuffleInterval: Int = 20,
    var shuffleDelayAfterKill: Int = 0,
    var enableCapture: Boolean = true,
    var captureTimeNeeded: Int = 0,
    var respawnEffectNBTs: List<String> = listOf("{ambient:0b,amplifier:255b,duration:100,id:resistance,show_icon:0b,show_particles:0b}"),
)

@Serializable
class BonusPlatformsConfigEntry(
    var enableBonusPlatforms: Boolean = true,
    var platforms: PlatformList = PlatformList(listOf(Platform("bonus1", BlockPos(0, 0, 0)))),
    var radius: Int = 0,
    var spawnInterval: Int = 0,
    var stayTime: Int = 0,
    var initialSpawn: Int = 0,
    var moneyAmount: Int = 0,
    var moneyInterval: Int = 0
)

@Serializable
class RespawnsConfigEntry(
    var perTeam: Int = 0
)

@Serializable
class MoneyConfigEntry(
    var start: Int = 0,
    var amountPerInterval: Int = 0,
    var interval: Int = 20,
    var perKill: Int = 0,
    var perKillStreakKill: Int = 0
)

@Serializable
class ShopSettingsConfigEntry(
    var shopBounds: List<BlockCuboid> = listOf(BlockCuboid(BlockPos(0, 0, 0), BlockPos(0, 0, 0))),
    var tpOutOfShopAfter: Int = 0,
    var refundPercent: Int = 0
)

// TODO: disable fall damage option

@Serializable
class MiscConfigEntry(
    var enableReveal: Boolean = true,
    var revealTimePerPlayer: Int = 0,
    var shopCloseTimeAfterReveal: Int = 0,
    var killStreakPenaltyCap: Int = 1,
    var startInShop: Boolean = false,
    var startInShopTpAfterSeconds: Int = 0,
)

@Serializable
class DisplayedTextConfigEntry(
    var currency: String = "$%amount"
)