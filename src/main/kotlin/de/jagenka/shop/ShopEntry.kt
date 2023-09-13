package de.jagenka.shop

import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity

interface ShopEntry
{
    val nameForStat: String

    fun getPrice(player: ServerPlayerEntity): Int
    fun getDisplayItemStack(player: ServerPlayerEntity): ItemStack

    /**
     * this method is called, when a player clicks on this shop entry.
     * @return if the process was legal/successful
     */
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
     * for refund: only refundable if true
     */
    fun hasItem(player: ServerPlayerEntity): Boolean

    /**
     * what to do if refunding
     */
    fun removeItem(player: ServerPlayerEntity)
}