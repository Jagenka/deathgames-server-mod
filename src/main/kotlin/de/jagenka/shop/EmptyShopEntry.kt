package de.jagenka.shop

import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity

class EmptyShopEntry : ShopEntry // TODO: test
{
    override val nameForStat: String
        get() = "EMPTY"

    override fun getPrice(player: ServerPlayerEntity): Int = 0

    override fun getDisplayItemStack(player: ServerPlayerEntity): ItemStack = ItemStack.EMPTY

    override fun buy(player: ServerPlayerEntity): Boolean = false
}