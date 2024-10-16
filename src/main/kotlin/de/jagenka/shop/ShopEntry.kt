package de.jagenka.shop

import de.jagenka.managers.DisplayManager.sendPrivateMessage
import de.jagenka.managers.PlayerManager
import de.jagenka.managers.deductDGMoney
import de.jagenka.managers.getDGMoney
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity

abstract class ShopEntry(internal val playerName: String, internal val nameForStat: String)
{
    val player: ServerPlayerEntity?
        get() = PlayerManager.getOnlinePlayer(playerName)

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

    companion object
    {
        /**
         * central method to attempt a sale to a player
         * @return if sale was successful
         */
        fun attemptSale(player: ServerPlayerEntity?, price: Int, saleProcess: () -> Unit): Boolean
        {
            if (player == null) return false
            if (getDGMoney(player.name.string) >= price)
            {
                saleProcess.invoke()
                deductDGMoney(player.name.string, price)
                return true
            } else
            {
                player.sendPrivateMessage(Shop.getNotEnoughMoneyString(price))
            }
            return false
        }
    }
}