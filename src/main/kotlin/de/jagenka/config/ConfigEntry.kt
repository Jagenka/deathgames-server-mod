package de.jagenka.config

import de.jagenka.*
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
    @Section("traps") val traps: TrapConfigEntry = TrapConfigEntry(),
    @Section("displayedText") val displayedText: DisplayedTextConfigEntry = DisplayedTextConfigEntry()
)

@Serializable
class GeneralConfigEntry(
    var enabled: Boolean = false,
    var locale: String = "en"
)

@Serializable
class SpawnsConfigEntry(
    var spawnPositions: CoordinateList = CoordinateList(listOf(Coordinates(0, 0, 0, 1f, 1f), Coordinates(0, 0, 0, 1f, 1f))),
    var platformRadius: Int = 0,
    var spectatorSpawn: Coordinates = Coordinates(0, 0, 0, 1f, 1f),
    var lobbySpawn: Coordinates = Coordinates(0, 0, 0, 1f, 1f),
    var shuffleInterval: Int = 20,
    var shuffleDelayAfterKill: Int = 0,
    var captureTimeNeeded: Int = 0,
    var captureEnabled: Boolean = false
)

@Serializable
class BonusPlatformsConfigEntry(
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
    var perPlayer: Int = 0,
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

@Serializable
class MiscConfigEntry(
    var revealTimePerPlayer: Int = 0,
    var shopCloseTimeAfterReveal: Int = 0,
    var killStreakPenaltyCap: Int = 1,
    var arenaBounds: BlockCuboid = BlockCuboid(BlockPos(0, 0, 0), BlockPos(0, 0, 0)),
    var spectatorRadiusPadding: Int = 0,
    var lobbyBounds: BlockCuboid = BlockCuboid(BlockPos(0, 0, 0), BlockPos(0, 0, 0))
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

@Serializable
class DisplayedTextConfigEntry(
    var startTitle: String = "Good Luck",
    var startSubtitle: String = "and have fun",
    var endTitle: String = "Game Over",
    var winnerSingular: String = "Winner",
    var winnerPlural: String = "Winners",
    var forceRespawned: String = "You have been force-respawned.",
    var shutdown: String = "Shutdown! %deceased was on a kill streak of %killStreak.",
    var receiveMoneyPlayer: String = "You receive %amount.",
    var receiveMoneyTeam: String = "Your team receives %amount.",
    var currency: String = "$%amount",
    var notEnoughMoney: String = "You do not have the required %amount.",
    var shopWindowTitle: String = "SHOP",
    var shopEnteredTitle: String = "Welcome to the shop!",
    var shopEnteredSubtitle: String = "Press F to pay money.",
    var shopClosedTitle: String = "Shop's closed!",
    var shopClosedSubtitle: String = "a K/D change might help",
    var shopTpOut: String = "You will be teleported out in %seconds seconds.",
    var refundItemText: String = "Refund %item for %amount",
    var bonusInactive: String = "Bonus Money Platform: Spawning in %timesec",
    var bonusInactiveWithName: String = "Bonus Money Platform: %name in %timesec",
    var bonusActive: String = "Bonus Money Platform: Active for another %timesec",
    var bonusActiveWithName: String = "Bonus Money Platform: %name for another %timesec"
)