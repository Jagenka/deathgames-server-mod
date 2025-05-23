package de.jagenka.config

import de.jagenka.timer.seconds
import kotlinx.serialization.Serializable

@Serializable
class ShopConfig(
    var items: List<Item> = listOf(Item()),
    var shield: Shield? = Shield(),
    var extraLife: ExtraLife? = ExtraLife(),
    var leaveShop: LeaveShop? = LeaveShop(),
    var upgrades: List<Upgrade> = listOf(Upgrade()),
    var refunds: List<Refund> = listOf(Refund()),
    var traps: List<Trap> = listOf(Trap()),
    var refundRecent: RefundRecent? = RefundRecent(),
    var hook: GrapplingHook? = GrapplingHook()
)

@Serializable
data class Item(
    var row: Int = 0,
    var col: Int = 0,
    var name: String = "NONAME",
    var id: String = "minecraft:fishing_rod",
    var amount: Int = 1,
    var components: String = "",
    var price: Int = 69,
)

@Serializable
data class Shield(
    var row: Int = 0,
    var col: Int = 1,
    var name: String = "NONAME",
    var durability: Int = 120,
    var price: Int = 0,
)

@Serializable
data class Upgrade(
    var row: Int = 0,
    var col: Int = 2,
    var name: String = "NONAME",
    var id: String = "bow",
    var levels: List<UpgradeLvl> = listOf(UpgradeLvl()),
)

@Serializable
data class UpgradeLvl(
    var items: List<UpgradeItem> = listOf(UpgradeItem()),
    var price: Int = 420,
)

@Serializable
data class UpgradeItem(
    var id: String = "minecraft:bow",
    var amount: Int = 1,
    var components: String = "",
)

@Serializable
data class Refund(
    var row: Int = 0,
    var col: Int = 3,
    var targetRow: Int = 0,
    var targetCol: Int = 2,
)

@Serializable
data class ExtraLife(
    var row: Int = 0,
    var col: Int = 4,
    var name: String = "NONAME",
    var id: String = "minecraft:turtle_egg",
    var amount: Int = 1,
    var components: String = "",
    var price: Int = 69,
)

@Serializable
data class LeaveShop(
    var row: Int = 0,
    var col: Int = 8,
)

@Serializable
data class Trap(
    var row: Int = 0,
    var col: Int = 5,
    var name: String = "NONAME",
    var price: Int = 69,
    var snare: Boolean = true,
    var effectNBTs: List<String> = listOf("{ambient:0b,amplifier:100b,duration:60,id:blindness,show_icon:0b,show_particles:0b}"),
    var triggerRange: Double = 0.5,
    var setupTime: Int = 10.seconds(),
    var triggerVisibilityRange: Double = 30.0,
    var visibilityRange: Double = 10.0,
    var affectedRange: Double = 1.5,
    var triggerDuration: Int = 6.seconds(),
)

@Serializable
data class RefundRecent(
    var row: Int = 3,
    var col: Int = 5,
    var name: String = "NONAME",
)
    
@Serializable
data class GrapplingHook(
    var row: Int = 2,
    var col: Int = 5,
    var name: String = "NONAME",
    var price: Int = 0,
    var maxDistance: Double = 40.0,   // In Blocks
    var cooldown: Int = 40,     // In ticks
)