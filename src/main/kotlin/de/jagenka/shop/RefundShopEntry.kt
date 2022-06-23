package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.config.Config
import de.jagenka.floor
import de.jagenka.managers.deductDGMoney
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text

class RefundShopEntry(private val shopEntryToRefund: ShopEntry) : ShopEntry
{
    override fun getPrice(player: ServerPlayerEntity): Int = 0

    override fun getDisplayItemStack(player: ServerPlayerEntity): ItemStack
    {
        val itemStackToDisplay: ItemStack = if (shopEntryToRefund is UpgradeableShopEntry) shopEntryToRefund.getPreviousDisplayItemStack(player).copy()
        else shopEntryToRefund.getDisplayItemStack(player).copy()

        return itemStackToDisplay
            .setCustomName(
                Text.of("Refund ${shopEntryToRefund.getDisplayName()} for ${Shop.SHOP_UNIT}${getRefundAmount(player)}").getWithStyle(
                    Style.EMPTY.withColor(
                        Util.getTextColor(255, 255, 255)
                    )
                )[0]
            )
    }

    override fun buy(player: ServerPlayerEntity): Boolean
    {
        return if (shopEntryToRefund.hasItem(player))
        {
            player.deductDGMoney(-getRefundAmount(player))
            shopEntryToRefund.removeItem(player)
            true
        } else false
    }

    private fun getRefundAmount(player: ServerPlayerEntity) = (shopEntryToRefund.getTotalSpentMoney(player) * (Config.refundPercent.toDouble() / 100.0)).floor()

    override val nameForStat: String
        get() = "${shopEntryToRefund.nameForStat}_REFUND"
}