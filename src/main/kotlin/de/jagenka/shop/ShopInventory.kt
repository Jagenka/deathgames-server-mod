package de.jagenka.shop

import de.jagenka.managers.scaledForRefund
import de.jagenka.stats.StatManager
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack

class ShopInventory(private val playerName: String) : Inventory
{
    private val items = mutableMapOf<Int, ShopEntry>().withDefault { ShopEntries.getShopFor(playerName).EMPTY }

    init
    {
        items.putAll(ShopEntries.getShopFor(playerName).entries)
    }

    override fun clear()
    {
    }

    override fun size() = Shop.SLOT_AMOUNT // has to be fixed for display to show

    override fun isEmpty() = items.isEmpty()

    override fun getStack(slot: Int): ItemStack
    {
        return items.getValue(slot).getDisplayItemStack()
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack
    {
        return ItemStack.EMPTY
    }

    override fun removeStack(slot: Int): ItemStack
    {
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

    }

    fun onClick(slotIndex: Int)
    {
        val shopEntry = items.getValue(slotIndex)
        if (isNonEmptySlot(slotIndex))
        {
            val moneySpent =
                if (shopEntry is RefundShopEntry) -shopEntry.getTotalSpentMoney().scaledForRefund() // - because refund
                else shopEntry.getPrice()

            if (shopEntry.onClick())
            {
                Shop.registerRecentlyBought(playerName, shopEntry)
                StatManager.addBoughtItem(playerName, shopEntry, moneySpent)
            }
        }
    }

    private fun isNonEmptySlot(slotIndex: Int) = items[slotIndex] != null
}