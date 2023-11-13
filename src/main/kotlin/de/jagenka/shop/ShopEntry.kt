package de.jagenka.shop

import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity

abstract class ShopEntry(internal val player: ServerPlayerEntity, internal val nameForStat: String)
{
    /**
     * display name for shop
     */
    internal open var displayName = nameForStat

    /**
     * @return current price
     */
    abstract fun getPrice(): Int

    /**
     * total money spent for refund
     */
    open fun getTotalSpentMoney(): Int = getPrice()

    /**
     * @return what item to currently display in shop
     */
    open fun getDisplayItemStack(): ItemStack = ItemStack(Items.STONE_BUTTON)

    /**
     * this method is called, when a player clicks on this shop entry.
     * @return if the process was legal/successful
     */
    open fun onClick(): Boolean = true

    /**
     * for refund: only refundable if true
     */
    abstract fun hasGoods(): Boolean

    /**
     * removes item from player's inventory
     */
    abstract fun removeGoods()
}