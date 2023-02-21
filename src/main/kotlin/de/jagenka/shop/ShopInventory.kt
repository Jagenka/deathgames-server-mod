package de.jagenka.shop

import de.jagenka.stats.StatManager
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity

class ShopInventory(private val player: ServerPlayerEntity) : Inventory
{
    private val items = mutableMapOf<Int, ShopEntry>().withDefault { ShopEntries.EMPTY }

    init
    {
        items.putAll(ShopEntries.shopEntries)
    }

    override fun clear()
    {
    }

    override fun size() = Shop.slotAmount // has to be fixed for display to show

    override fun isEmpty() = items.isEmpty()

    override fun getStack(slot: Int): ItemStack
    {
        return items.getValue(slot).getDisplayItemStack(player)
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
            val moneySpent = if (shopEntry is RefundShopEntry) -shopEntry.getRefundAmount(player) else shopEntry.getPrice(player)

            if (shopEntry.buy(player))
            {
                Shop.registerBought(player.name.string, shopEntry)
                StatManager.addBoughtItem(player.name.string, shopEntry, moneySpent)
            }
        }
    }

    private fun isNonEmptySlot(slotIndex: Int) = items[slotIndex] != null
}