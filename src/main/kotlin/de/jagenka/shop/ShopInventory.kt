package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.Util.sendPrivateMessage
import de.jagenka.deductDGMoney
import de.jagenka.getDGMoney
import de.jagenka.shop.Shop.SHOP_UNIT
import de.jagenka.timer.seconds
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items.*
import net.minecraft.potion.PotionUtil
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text

enum class ShopEntryType
{
    ITEM, SWORD, AXE, BOW, CROSSBOW, ARMOR, LIFE
}

class ShopEntry(private val boughtItemStack: ItemStack, val price: Int, val name: String)
{
    val displayItemStack: ItemStack =
        boughtItemStack.copy().setCustomName(
            Text.of("$SHOP_UNIT$price: $name x${boughtItemStack.count}").getWithStyle(Style.EMPTY.withColor(Util.getTextColor(255, 255, 255)))[0]
        ) //TODO: make fancy

    fun getDisplayItemStackWithFormatting(player: ServerPlayerEntity): ItemStack = displayItemStack.copy()
        .setCustomName(
            Text.of("$SHOP_UNIT$price: $name x${boughtItemStack.count}").getWithStyle(
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
            0 to ShopEntry(ItemStack(MELON_SLICE, 8), 5, "Melon"),
            1 to ShopEntry(ItemStack(ARROW, 4), 5, "Arrows"),
            2 to ShopEntry(ItemStack(ENDER_PEARL, 2), 10, "Ender Pearls"),
            3 to ShopEntry(ItemStack(COOKED_BEEF, 1), 10, "Beef"),
            4 to ShopEntry(ItemStack(FISHING_ROD), 30, "Fishing Rod"),
            5 to ShopEntry(ItemStack(BLAZE_ROD), 40, "420 Blaze it"), // TODO: add fire aspect
            6 to ShopEntry(PotionUtil.setCustomPotionEffects(POTION.defaultStack, listOf(StatusEffectInstance(StatusEffects.INVISIBILITY, 5.seconds()))), 35, "Invis"),
            7 to ShopEntry(ItemStack(POTION), 30, "Speed"),
            8 to ShopEntry(ItemStack(POTION), 20, "Fire Res"),
            9 to ShopEntry(ItemStack(POTION), 45, "Strength"),
            10 to ShopEntry(ItemStack(POTION), 25, "Regen"),
            11 to ShopEntry(ItemStack(POTION), 20, "Harming"), // lingering
            12 to ShopEntry(ItemStack(POTION), 40, "Slowness"),
            13 to ShopEntry(ItemStack(GOLDEN_APPLE), 150, "Gold Apple"),
            14 to ShopEntry(ItemStack(TOTEM_OF_UNDYING), 150, "Totem"),
            15 to ShopEntry(ItemStack(TIPPED_ARROW, 4), 25, "Poison Arrow"), //TODO: add poison
//            16 to ShopEntry(ItemStack(IRON_CHESTPLATE), 0, "Armor Upgrade"),
//            17 to ShopEntry(ItemStack(BOW), 0, "Bow Upgrade"),
//            18 to ShopEntry(ItemStack(CROSSBOW), 0, "Crossbow Upgrade"),
//            19 to ShopEntry(ItemStack(IRON_SWORD), 0, "Sword Upgrade"),
//            20 to ShopEntry(ItemStack(IRON_AXE), 0, "Axe Upgrade"),
            21 to ShopEntry(ItemStack(BAT_SPAWN_EGG, 1), 10, "Not Gay"),
            22 to ShopEntry(ItemStack(SHIELD), 50, "Shield"),
//            23 to ShopEntry(ItemStack(TURTLE_EGG), 100, "Extra Life"),
            24 to ShopEntry(ItemStack(TRIDENT), 69_420, "Trident"),
            25 to ShopEntry(ItemStack(MILK_BUCKET), 5, "An Lüter Mülsch")
        )
    }
}

class ShopInventory(private val player: ServerPlayerEntity) : Inventory
{
    private val items = mutableMapOf<Int, ShopEntry>().withDefault { ShopEntry.EMPTY }

    init
    {
        items.putAll(ShopEntry.shopEntries)
    }

    override fun clear()
    {
    }

    override fun size() = 36 // has to be fixed for display to show

    override fun isEmpty() = items.isEmpty()

    override fun getStack(slot: Int): ItemStack
    {
        return items.getValue(slot).getDisplayItemStackWithFormatting(player)
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack
    {
        onClick(slot)
        return ItemStack.EMPTY
    }

    override fun removeStack(slot: Int): ItemStack
    {
        onClick(slot)
        return ItemStack.EMPTY
    }

    override fun setStack(slot: Int, stack: ItemStack?)
    {
    }

    override fun markDirty()
    {
    }

    override fun canPlayerUse(player: PlayerEntity?) = true

    override fun onOpen(player: PlayerEntity?)
    {
        //TODO: start timer?
    }

    fun onClick(slotIndex: Int)
    {
        val shopEntry = items.getValue(slotIndex)
        if (isNonEmptySlot(slotIndex)) //TODO: money
        {
            if (player.getDGMoney() >= shopEntry.price)
            {
                player.giveItemStack(shopEntry.getItemStackToGive())
                player.deductDGMoney(shopEntry.price)
            } else
            {
                player.sendPrivateMessage("You do not have the required $SHOP_UNIT${shopEntry.price}")
            }
        }
    }

    private fun isNonEmptySlot(slotIndex: Int) = items[slotIndex] != null
}