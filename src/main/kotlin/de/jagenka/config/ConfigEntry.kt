package de.jagenka.config

import de.jagenka.BlockCuboid
import de.jagenka.BlockPos
import de.jagenka.Coordinates
import de.jagenka.PlatformList
import de.jagenka.managers.DGSpawn
import de.jagenka.managers.Platform
import de.jagenka.team.DGTeam
import de.jagenka.team.DGTeam.*
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
    @Section("shopSettings") val shopSettings: ShopSettingsConfigEntry = ShopSettingsConfigEntry(),
    @Section("misc") val misc: MiscConfigEntry = MiscConfigEntry(),
    @Section("displayedText") val displayedText: DisplayedTextConfigEntry = DisplayedTextConfigEntry(),
    @Section("shop") val shop: ShopConfig = ShopConfig()
)

@Serializable
class GeneralConfigEntry(
    var enabled: Boolean = false,
    var locale: String = "en",
    var arenaBounds: BlockCuboid = BlockCuboid(BlockPos(-10, -10, -10), BlockPos(10, 10, 10)),
    var spectatorRadiusPadding: Int = 10,
    var lobbyBounds: BlockCuboid = BlockCuboid(BlockPos(-10, 20, -10), BlockPos(10, 30, 10)),
    var enabledTeams: List<DGTeam> = listOf(BLACK, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW)
)

@Serializable
class SpawnsConfigEntry(
    var spawnPositions: List<DGSpawn> = listOf(DGSpawn(Coordinates(0, 0, 0, 0f, 0f), defaultOwner = BLACK)),
    var platformRadius: Int = 4,
    var spectatorSpawn: Coordinates = Coordinates(0, 5, 0, 0f, 0f),
    var lobbySpawn: Coordinates = Coordinates(0, 25, 0, 0f, 0f),
    var enableShuffle: Boolean = false,
    var shuffleInterval: Int = 2400,
    var shuffleDelayAfterKill: Int = 200,
    var enableCapture: Boolean = true,
    var captureTimeNeeded: Int = 400,
    var respawnEffectNBTs: List<String> = listOf("{ambient:0b,amplifier:255b,duration:100,id:resistance,show_icon:0b,show_particles:0b}"),
)

@Serializable
class BonusPlatformsConfigEntry(
    var enableBonusPlatforms: Boolean = true,
    var platforms: PlatformList = PlatformList(listOf(Platform("bonus1", BlockPos(7, 0, 0)))),
    var radius: Int = 2,
    var spawnInterval: Int = 3600,
    var stayTime: Int = 2400,
    var initialSpawn: Int = 1800,
    var moneyAmount: Int = 5,
    var moneyInterval: Int = 200
)

@Serializable
class RespawnsConfigEntry(
    var perTeam: Int = 10
)

@Serializable
class MoneyConfigEntry(
    var start: Int = 110,
    var amountPerInterval: Int = 5,
    var interval: Int = 600,
    var perKill: Int = 20,
    var perKillStreakKill: Int = 10
)

@Serializable
class ShopSettingsConfigEntry(
    var shopBounds: List<BlockCuboid> = listOf(BlockCuboid(BlockPos(-10, -10, -10), BlockPos(-5, -5, -5))),
    var tpOutOfShopAfter: Int = 600,
    var refundPercent: Int = 100
)

@Serializable
class MiscConfigEntry(
    var enableReveal: Boolean = true,
    var revealTimePerPlayer: Int = 2400,
    var shopCloseTimeAfterReveal: Int = 2400,
    var killStreakPenaltyCap: Int = 10,
    var startInShop: Boolean = true,
    var startInShopTpAfterSeconds: Int = 30,
    var enableFallDamage: Boolean = true,
    var freezeTime: Boolean = true,
    var timeAtGameStart: Long = 6000,
)

@Serializable
class DisplayedTextConfigEntry(
    var currency: String = "$%amount"
)