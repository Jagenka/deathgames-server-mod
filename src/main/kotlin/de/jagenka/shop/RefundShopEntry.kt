package de.jagenka.shop

import de.jagenka.Util
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
                Text.of("Refund ${shopEntryToRefund.getDisplayName()} for ${Shop.SHOP_UNIT}${shopEntryToRefund.getTotalSpentMoney(player)}").getWithStyle(
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
            player.deductDGMoney(-shopEntryToRefund.getTotalSpentMoney(player))
            shopEntryToRefund.removeItem(player)
            true
        } else false
    }
}