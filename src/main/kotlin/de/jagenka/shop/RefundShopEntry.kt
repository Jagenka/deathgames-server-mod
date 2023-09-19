package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.managers.MoneyManager
import de.jagenka.managers.refundMoney
import de.jagenka.managers.scaledForRefund
import de.jagenka.util.I18n
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text

class RefundShopEntry(player: ServerPlayerEntity, private val row: Int, private val col: Int) :
    ShopEntry(
        player = player,
        nameForStat = "${(ShopEntries.getShopFor(player).entries[ShopEntries.slot(row, col)] ?: EmptyShopEntry(player)).nameForStat}_REFUND"
    ) // cannot access shopEntryToRefund yet...
{
    val shopEntryToRefund: ShopEntry
        get() = ShopEntries.getShopFor(player).entries[ShopEntries.slot(row, col)] ?: EmptyShopEntry(player)

    override fun getPrice(): Int = 0

    override fun getDisplayItemStack(): ItemStack
    {
        val itemStackToDisplay = (shopEntryToRefund as? UpgradeableShopEntry)?.let { entry ->
            entry.getCurrentLevelDisplayItemStack().copy()
        } ?: shopEntryToRefund.getDisplayItemStack().copy()

        return itemStackToDisplay
            .setCustomName(//"Refund ${shopEntryToRefund.getDisplayName()} for ${MoneyManager.getCurrencyString(getRefundAmount(player))}"
                Text.of(
                    I18n.get(
                        "refundItemText",
                        mapOf("item" to shopEntryToRefund.displayName, "amount" to MoneyManager.getCurrencyString(shopEntryToRefund.getTotalSpentMoney().scaledForRefund()))
                    )
                ).getWithStyle(
                    Style.EMPTY.withColor(
                        Util.getTextColor(255, 255, 255)
                    )
                )[0]
            )
    }

    override fun onClick(): Boolean
    {
        super.onClick()

        return if (shopEntryToRefund.hasGoods())
        {
            player.refundMoney(shopEntryToRefund.getTotalSpentMoney())
            shopEntryToRefund.removeGoods()
            true
        } else false
    }

    override fun toString(): String
    {
        return "row$row col$col refund"
    }

    override fun hasGoods(): Boolean = false // this ShopEntry is not refundable
    override fun removeGoods() = Unit // refund should do nothing
}