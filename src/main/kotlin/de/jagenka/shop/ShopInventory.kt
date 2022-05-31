package de.jagenka.shop

import de.jagenka.Util
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
import net.minecraft.text.TextColor

enum class ShopEntryType
{
    ITEM, SWORD, AXE, BOW, CROSSBOW, ARMOR, LIFE
}

class ShopEntry(val boughtItemStack: ItemStack, val price: Int, val name: String)
{
    val displayItemStack: ItemStack =
        boughtItemStack.copy().setCustomName(Text.of("$SHOP_UNIT$price: $name").getWithStyle(Style.EMPTY.withColor(Util.getTextColor(255, 255, 255)))[0])

    companion object
    {
        val EMPTY = ShopEntry(ItemStack.EMPTY, 0, "")

        val shopEntries = mapOf(
            0 to ShopEntry(ItemStack(MELON_SLICE), 5, "Melon"),
            1 to ShopEntry(ItemStack(ARROW), 5, "Arrows"),
            2 to ShopEntry(ItemStack(ENDER_PEARL), 10, "Ender Pearls"),
            3 to ShopEntry(ItemStack(COOKED_BEEF), 10, "Beef"),
            4 to ShopEntry(ItemStack(FISHING_ROD), 30, "Fishing Rod"),
            5 to ShopEntry(ItemStack(BLAZE_ROD), 40, "420 Blaze it"),
            6 to ShopEntry(PotionUtil.setCustomPotionEffects(POTION.defaultStack, listOf(StatusEffectInstance(StatusEffects.INVISIBILITY, 5.seconds()))), 35, "Invis"),
            7 to ShopEntry(ItemStack(POTION), 30, "Speed"),
            8 to ShopEntry(ItemStack(POTION), 20, "Fire Res"),
            9 to ShopEntry(ItemStack(POTION), 45, "Strength"),
            10 to ShopEntry(ItemStack(POTION), 25, "Regen"),
            11 to ShopEntry(ItemStack(POTION), 20, "Harming"), // lingering
            12 to ShopEntry(ItemStack(POTION), 40, "Slowness"),
            13 to ShopEntry(ItemStack(GOLDEN_APPLE), 150, "Gold Apple"),
            14 to ShopEntry(ItemStack(TOTEM_OF_UNDYING), 150, "Totem"),
            15 to ShopEntry(ItemStack(TIPPED_ARROW), 25, "Poison Arrow"),
            16 to ShopEntry(ItemStack(IRON_CHESTPLATE), 0, "Armor Upgrade"),
            17 to ShopEntry(ItemStack(BOW), 0, "Bow Upgrade"),
            18 to ShopEntry(ItemStack(CROSSBOW), 0, "Crossbow Upgrade"),
            19 to ShopEntry(ItemStack(IRON_SWORD), 0, "Sword Upgrade"),
            20 to ShopEntry(ItemStack(IRON_AXE), 0, "Axe Upgrade"),
            21 to ShopEntry(ItemStack(BAT_SPAWN_EGG), 10, "Not Gay"),
            22 to ShopEntry(ItemStack(SHIELD), 50, "Shield"),
            23 to ShopEntry(ItemStack(TURTLE_EGG), 100, "Extra Life"),
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

    private fun initSlot(index: Int): ItemStack //TODO: load from config
    {
        return when (index)
        {
            0 -> ItemStack(MELON_SLICE).setCustomName(Text.of("Melon"))
            1 -> ItemStack(ARROW).setCustomName(Text.of("Arrows"))
            2 -> ItemStack(ENDER_PEARL).setCustomName(Text.of("Ender Pearls"))
            3 -> ItemStack(COOKED_BEEF).setCustomName(Text.of("Beef"))
            4 -> ItemStack(FISHING_ROD).setCustomName(Text.of("Fishing Rod"))
            5 -> ItemStack(BLAZE_ROD).setCustomName(Text.of("420 Blaze it"))
            6 -> PotionUtil.setCustomPotionEffects(POTION.defaultStack, listOf(StatusEffectInstance(StatusEffects.INVISIBILITY, 5.seconds()))).setCustomName(Text.of("Invis"))
            7 -> ItemStack(POTION).setCustomName(Text.of("Speed"))
            8 -> ItemStack(POTION).setCustomName(Text.of("Fire Res"))
            9 -> ItemStack(POTION).setCustomName(Text.of("Strength"))
            10 -> ItemStack(POTION).setCustomName(Text.of("Regen"))
            11 -> ItemStack(POTION).setCustomName(Text.of("Harming"))
            12 -> ItemStack(POTION).setCustomName(Text.of("Slowness"))
            13 -> ItemStack(GOLDEN_APPLE).setCustomName(Text.of("Gold Apple"))
            14 -> ItemStack(TOTEM_OF_UNDYING).setCustomName(Text.of("Totem"))
            15 -> ItemStack(TIPPED_ARROW).setCustomName(Text.of("Poison Arrow"))
            16 -> ItemStack(IRON_CHESTPLATE).setCustomName(Text.of("Armor Upgrade"))
            17 -> ItemStack(BOW).setCustomName(Text.of("Bow Upgrade"))
            18 -> ItemStack(CROSSBOW).setCustomName(Text.of("Crossbow Upgrade"))
            19 -> ItemStack(IRON_SWORD).setCustomName(Text.of("Sword Upgrade"))
            20 -> ItemStack(IRON_AXE).setCustomName(Text.of("Axe Upgrade"))
            21 -> ItemStack(BAT_SPAWN_EGG).setCustomName(Text.of("Not Gay"))
            22 -> ItemStack(SHIELD).setCustomName(Text.of("Shield"))
            23 -> ItemStack(TURTLE_EGG).setCustomName(Text.of("Extra Life"))
            24 -> ItemStack(TRIDENT).setCustomName(Text.of("Trident"))
            25 -> ItemStack(MILK_BUCKET).setCustomName(Text.of("An Lüter Mülsch"))
            else -> ItemStack.EMPTY
        }
    }

    override fun clear()
    {
    }

    override fun size() = 27 // has to be fixed for display to show

    override fun isEmpty() = items.isEmpty()

    override fun getStack(slot: Int): ItemStack
    {
        return items.getValue(slot).displayItemStack
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
        //TODO: start timer
    }

    fun onClick(slotIndex: Int)
    {
        println("${player.name.asString()} clicked on ${items.getValue(slotIndex).displayItemStack.name.asString()} costing ${items.getValue(slotIndex).price} $SHOP_UNIT")
        if (isNonEmptySlot(slotIndex))
        {
            player.closeHandledScreen()
            Shop.showInterface(player)
        }
    }

    private fun isNonEmptySlot(slotIndex: Int) = items[slotIndex] != null
}