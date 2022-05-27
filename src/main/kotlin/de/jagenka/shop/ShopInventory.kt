package de.jagenka.shop

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class ShopInventory(private val player: ServerPlayerEntity) : Inventory
{
    private val items = mutableMapOf<Int, ItemStack>()

    init
    {
        repeat(25) {
            items[it] = ItemStack(Items.COMPASS).setCustomName(Text.of(it.toString()))
        }
    }

    override fun clear()
    {
    }

    override fun size() = 27

    override fun isEmpty() = items.isEmpty()

    override fun getStack(slot: Int): ItemStack
    {
        if (slot >= items.size) return ItemStack.EMPTY
        return items[slot] ?: ItemStack.EMPTY
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
        println("${player.name.asString()} clicked slot $slotIndex")
        if (isNonEmptySlot(slotIndex))
        {
            player.closeHandledScreen()
            Shop.showInterface(player)
        }
    }

    fun isNonEmptySlot(slotIndex: Int) = items[slotIndex] != null
}