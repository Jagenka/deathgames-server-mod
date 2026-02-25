package de.jagenka.shop

import de.jagenka.managers.scaledForRefund
import de.jagenka.stats.StatManager
import net.minecraft.world.Container
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

class ShopInventory(private val playerName: String) : Container
{
    private val items = mutableMapOf<Int, ShopEntry>().withDefault { ShopEntries.getShopFor(playerName).EMPTY }

    init
    {
        items.putAll(ShopEntries.getShopFor(playerName).entries)
    }

    override fun clearContent()
    {
    }

    override fun getContainerSize() = Shop.SLOT_AMOUNT // has to be fixed for display to show

    override fun isEmpty() = items.isEmpty()

    override fun getItem(slot: Int): ItemStack
    {
        return items.getValue(slot).getDisplayItemStack()
    }

    override fun removeItem(slot: Int, amount: Int): ItemStack
    {
        return ItemStack.EMPTY
    }

    override fun removeItemNoUpdate(slot: Int): ItemStack
    {
        return ItemStack.EMPTY
    }

    override fun setItem(slot: Int, stack: ItemStack)
    {
    }

    override fun setChanged()
    {
    }

    override fun stillValid(player: Player) = true

    fun onClick(slotIndex: Int)
    {
        val shopEntry = items.getValue(slotIndex)
        if (isNonEmptySlot(slotIndex))
        {
            val moneySpent =
                if (shopEntry is RefundShopEntry) shopEntry.getTotalSpentMoney().scaledForRefund()
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