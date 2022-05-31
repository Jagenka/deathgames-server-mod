package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.getDGMoney
import de.jagenka.timer.seconds
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.potion.PotionUtil
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text

class ShopEntry(private val boughtItemStack: ItemStack, val price: Int, val name: String)
{
    val displayItemStack: ItemStack =
        boughtItemStack.copy().setCustomName(
            Text.of("${Shop.SHOP_UNIT}$price: $name x${boughtItemStack.count}").getWithStyle(Style.EMPTY.withColor(Util.getTextColor(255, 255, 255)))[0]
        ) //TODO: make fancy

    fun getDisplayItemStackWithFormatting(player: ServerPlayerEntity): ItemStack = displayItemStack.copy()
        .setCustomName(
            Text.of("${Shop.SHOP_UNIT}$price: $name x${boughtItemStack.count}").getWithStyle(
                Style.EMPTY.withColor(
                    if (player.getDGMoney() < price) Util.getTextColor(123, 0, 0)
                    else Util.getTextColor(255, 255, 255)
                )
            )[0]
        )

    fun getItemStackToGive(): ItemStack = boughtItemStack.copy()

    companion object
    {
        val EMPTY = ShopEntry(ItemStack.EMPTY, 0, "")

        val shopEntries = mapOf( //TODO: load from config
            0 to ShopEntry(ItemStack(Items.MELON_SLICE, 8), 5, "Melon"),
            1 to ShopEntry(ItemStack(Items.ARROW, 4), 5, "Arrows"),
            2 to ShopEntry(ItemStack(Items.ENDER_PEARL, 2), 10, "Ender Pearls"),
            3 to ShopEntry(ItemStack(Items.COOKED_BEEF, 1), 10, "Beef"),
            4 to ShopEntry(ItemStack(Items.FISHING_ROD), 30, "Fishing Rod"),
            5 to ShopEntry(ItemStack(Items.BLAZE_ROD), 40, "420 Blaze it"), // TODO: add fire aspect
            6 to ShopEntry(PotionUtil.setCustomPotionEffects(Items.POTION.defaultStack, listOf(StatusEffectInstance(StatusEffects.INVISIBILITY, 5.seconds()))), 35, "Invis"),
            7 to ShopEntry(ItemStack(Items.POTION), 30, "Speed"),
            8 to ShopEntry(ItemStack(Items.POTION), 20, "Fire Res"),
            9 to ShopEntry(ItemStack(Items.POTION), 45, "Strength"),
            10 to ShopEntry(ItemStack(Items.POTION), 25, "Regen"),
            11 to ShopEntry(ItemStack(Items.POTION), 20, "Harming"), // lingering
            12 to ShopEntry(ItemStack(Items.POTION), 40, "Slowness"),
            13 to ShopEntry(ItemStack(Items.GOLDEN_APPLE), 150, "Gold Apple"),
            14 to ShopEntry(ItemStack(Items.TOTEM_OF_UNDYING), 150, "Totem"),
            15 to ShopEntry(ItemStack(Items.TIPPED_ARROW, 4), 25, "Poison Arrow"), //TODO: add poison
//            16 to ShopEntry(ItemStack(IRON_CHESTPLATE), 0, "Armor Upgrade"),
//            17 to ShopEntry(ItemStack(BOW), 0, "Bow Upgrade"),
//            18 to ShopEntry(ItemStack(CROSSBOW), 0, "Crossbow Upgrade"),
//            19 to ShopEntry(ItemStack(IRON_SWORD), 0, "Sword Upgrade"),
//            20 to ShopEntry(ItemStack(IRON_AXE), 0, "Axe Upgrade"),
            21 to ShopEntry(ItemStack(Items.BAT_SPAWN_EGG, 1), 10, "Not Gay"),
            22 to ShopEntry(ItemStack(Items.SHIELD), 50, "Shield"),
//            23 to ShopEntry(ItemStack(TURTLE_EGG), 100, "Extra Life"),
            24 to ShopEntry(ItemStack(Items.TRIDENT), 69_420, "Trident"),
            25 to ShopEntry(ItemStack(Items.MILK_BUCKET), 5, "An Lüter Mülsch")
        )
    }
}