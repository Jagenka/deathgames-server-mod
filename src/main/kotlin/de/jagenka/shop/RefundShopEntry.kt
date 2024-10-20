package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.managers.MoneyManager
import de.jagenka.managers.refundMoney
import de.jagenka.managers.scaledForRefund
import de.jagenka.setCustomName
import de.jagenka.util.I18n
import net.minecraft.item.ItemStack
import net.minecraft.text.Style
import net.minecraft.text.Text

class RefundShopEntry(playerName: String, private val shopEntryToRefund: ShopEntry) :
    ShopEntry(
        playerName,
        nameForStat = "${shopEntryToRefund.nameForStat}_REFUND"
    )
{
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
            refundMoney(playerName, shopEntryToRefund.getTotalSpentMoney())
            shopEntryToRefund.removeGoods()
            if (shopEntryToRefund is UpgradeableShopEntry)
            {
                Shop.clearRecentlyBought(playerName, shopEntryToRefund)
            } else
            {
                Shop.unregisterRecentlyBought(playerName, shopEntryToRefund) // this is for refund recent, so that already refunded items don't get (re)refunded again
            }
            true
        } else false
    }

    override fun toString(): String
    {
        return "${shopEntryToRefund.nameForStat} refund"
    }

    override fun hasGoods(): Boolean = false // this ShopEntry is not refundable
    override fun removeGoods() = Unit // refund should do nothing
}