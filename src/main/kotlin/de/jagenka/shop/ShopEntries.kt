package de.jagenka.shop

import de.jagenka.timer.seconds
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items.*
import net.minecraft.nbt.NbtCompound
import net.minecraft.potion.PotionUtil

object ShopEntries
{
    val EMPTY = ItemShopEntry(ItemStack.EMPTY, 0, "")

    val shopEntries = mapOf( //TODO: load from config
        0 to ItemShopEntry(ItemStack(MELON_SLICE, 8), 5, "Melon"),
        1 to ItemShopEntry(ItemStack(ARROW, 4), 5, "Arrows"),
        2 to ItemShopEntry(ItemStack(ENDER_PEARL, 2), 10, "Ender Pearls"),
        3 to ItemShopEntry(ItemStack(COOKED_BEEF, 1), 10, "Beef"),
        4 to ItemShopEntry(ItemStack(FISHING_ROD), 30, "Fishing Rod"),
        5 to ItemShopEntry(ItemStack(BLAZE_ROD), 40, "420 Blaze it"), // TODO: add fire aspect
        6 to ItemShopEntry(PotionUtil.setCustomPotionEffects(POTION.defaultStack, listOf(StatusEffectInstance(StatusEffects.INVISIBILITY, 5.seconds()))), 35, "Invis"),
        7 to ItemShopEntry(ItemStack(POTION), 30, "Speed"),
        8 to ItemShopEntry(ItemStack(POTION), 20, "Fire Res"),
        9 to ItemShopEntry(ItemStack(POTION), 45, "Strength"),
        10 to ItemShopEntry(ItemStack(POTION), 25, "Regen"),
        11 to ItemShopEntry(ItemStack(POTION), 20, "Harming"), // lingering
        12 to ItemShopEntry(ItemStack(POTION), 40, "Slowness"),
        13 to ItemShopEntry(ItemStack(GOLDEN_APPLE), 150, "Gold Apple"),
        14 to ItemShopEntry(ItemStack(TOTEM_OF_UNDYING), 150, "Totem"),
        15 to ItemShopEntry(ItemStack(TIPPED_ARROW, 4), 25, "Poison Arrow"), //TODO: add poison
//            16 to ShopEntry(ItemStack(IRON_CHESTPLATE), 0, "Armor Upgrade"),
//            17 to ShopEntry(ItemStack(BOW), 0, "Bow Upgrade"),
//            18 to ShopEntry(ItemStack(CROSSBOW), 0, "Crossbow Upgrade"),
//            19 to ShopEntry(ItemStack(IRON_SWORD), 0, "Sword Upgrade"),
//            20 to ShopEntry(ItemStack(IRON_AXE), 0, "Axe Upgrade"),
        21 to ItemShopEntry(ItemStack(BAT_SPAWN_EGG, 1), 10, "Not Gay"),
        22 to ItemShopEntry(ItemStack(SHIELD), 50, "Shield"),
//            23 to ShopEntry(ItemStack(TURTLE_EGG), 100, "Extra Life"),
        24 to ItemShopEntry(ItemStack(TRIDENT), 69_420, "Trident"),
        25 to ItemShopEntry(ItemStack(MILK_BUCKET), 5, "An Lüter Mülsch"),

        30 to UpgradeableShopEntry(
            UpgradeType.ARMOR,
            mutableListOf(
                mutableListOf(LEATHER_HELMET.unbreakable(), LEATHER_LEGGINGS.unbreakable()),
                mutableListOf(LEATHER_CHESTPLATE.unbreakable(), LEATHER_BOOTS.unbreakable()),
                mutableListOf(IRON_HELMET.unbreakable(), IRON_LEGGINGS.unbreakable()),
                mutableListOf(IRON_CHESTPLATE.unbreakable(), IRON_BOOTS.unbreakable()),
                mutableListOf(DIAMOND_HELMET.unbreakable(), DIAMOND_LEGGINGS.unbreakable()),
                mutableListOf(DIAMOND_CHESTPLATE.unbreakable(), DIAMOND_BOOTS.unbreakable()),
            ),
            mutableListOf(20, 40, 60, 80, 100, 120),
            "Armor Upgrade"
        )
    )

    private fun Item.unbreakable(): ItemStack = ItemStack(this).makeUnbreakable()

    private fun ItemStack.makeUnbreakable(): ItemStack
    {
        this.orCreateNbt.putInt("Unbreakable", 1)
        return this
    }
}