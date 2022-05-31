package de.jagenka.shop

import de.jagenka.Util.sendPrivateMessage
import de.jagenka.deductDGMoney
import de.jagenka.getDGMoney
import de.jagenka.shop.Shop.SHOP_UNIT
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity

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