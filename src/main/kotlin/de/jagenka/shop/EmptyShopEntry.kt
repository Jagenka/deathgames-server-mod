package de.jagenka.shop

import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity

/**
 * placeholder ShopEntry in case something goes wrong
 */
class EmptyShopEntry : ShopEntry
{
    override val nameForStat: String
        get() = "EMPTY"

    override fun getPrice(player: ServerPlayerEntity): Int = 0

    override fun getDisplayItemStack(player: ServerPlayerEntity): ItemStack = ItemStack.EMPTY

    override fun onClick(player: ServerPlayerEntity): Boolean = false

    override fun hasItem(player: ServerPlayerEntity): Boolean = false // this ShopEntry is not refundable
    override fun removeItem(player: ServerPlayerEntity) = Unit // refund should do nothing
}