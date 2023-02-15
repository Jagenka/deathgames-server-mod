package de.jagenka.config

import kotlinx.serialization.Serializable

@Serializable
class ShopConfig(
    var items: List<Item> = listOf(Item()),
    var shield: Shield = Shield(),
    var extraLive: ExtraLife = ExtraLife(),
    var leaveShop: LeaveShop = LeaveShop(),
    var upgrades: List<Upgrade> = listOf(Upgrade()),
    var refunds: List<Refund> = listOf(Refund()),
)

@Serializable
class Item(
    var row: Int = 0,
    var col: Int = 0,
    var name: String = "NONAME",
    var id: String = "minecraft:fishing_rod",
    var amount: Int = 1,
    var nbt: String = "",
    var price: Int = 69,
)

@Serializable
class Shield(
    var row: Int = 0,
    var col: Int = 1,
    var name: String = "NONAME",
    var durability: Int = 120,
)

@Serializable
class Upgrade(
    var row: Int = 0,
    var col: Int = 2,
    var name: String = "NONAME",
    var id: String = "bow",
    var levels: List<UpgradeLvl> = listOf(UpgradeLvl()),
)

@Serializable
class UpgradeLvl(
    var items: List<UpgradeItem> = listOf(UpgradeItem()),
    var price: Int = 420,
)

@Serializable
class UpgradeItem(
    var name: String = "NONAME",
    var id: String = "minecraft:bow",
    var amount: Int = 1,
    var nbt: String = "{Enchantments:[{id:power,lvl:3}]}",
)

@Serializable
class Refund(
    var row: Int = 0,
    var col: Int = 3,
    var targetRow: Int = 0,
    var targetCol: Int = 2,
)

@Serializable
class ExtraLife(
    var row: Int = 0,
    var col: Int = 4,
    var name: String = "NONAME",
    var id: String = "minecraft:turtle_egg",
    var amount: Int = 1,
    var nbt: String = "",
    var price: Int = 69,
)

@Serializable
class LeaveShop(
    var row: Int = 0,
    var col: Int = 8,
)