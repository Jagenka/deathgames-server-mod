package de.jagenka.shop

import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity

interface ShopEntry
{
    fun getPrice(player: ServerPlayerEntity): Int

    fun getDisplayItemStack(player: ServerPlayerEntity): ItemStack

    fun buy(player: ServerPlayerEntity): Boolean
}