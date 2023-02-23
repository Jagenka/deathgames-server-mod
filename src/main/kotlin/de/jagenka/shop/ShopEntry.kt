package de.jagenka.shop

import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity

interface ShopEntry
{
    val nameForStat: String

    fun getPrice(player: ServerPlayerEntity): Int
    fun getDisplayItemStack(player: ServerPlayerEntity): ItemStack
    fun buy(player: ServerPlayerEntity): Boolean

    /**
     * total money spent for refund
     */
    fun getTotalSpentMoney(player: ServerPlayerEntity): Int = getPrice(player)

    /**
     * display name for refund
     */
    fun getDisplayName(): String = ""

    /**
     * for refund: only refundable if true - refund is therefore disabled by default
     */
    fun hasItem(player: ServerPlayerEntity): Boolean = false

    /**
     * what to do if refunding
     */
    fun removeItem(player: ServerPlayerEntity)
    {
    }
}