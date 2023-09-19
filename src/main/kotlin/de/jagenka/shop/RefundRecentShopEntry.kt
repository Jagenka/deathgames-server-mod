package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.managers.deductDGMoney
import de.jagenka.stats.StatManager
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text

class RefundRecentShopEntry(player: ServerPlayerEntity, override var displayName: String = "Refund recent purchases") : ShopEntry(player = player, nameForStat = "refund_recent")
{
    override fun getPrice(): Int = 0

    override fun getDisplayItemStack(): ItemStack
    {
        return Items.NAME_TAG.defaultStack.copy()
            .setCustomName(
                Text.of(displayName).getWithStyle(
                    Style.EMPTY.withColor(
                        Util.getTextColor(255, 255, 255)
                    )
                )[0]
            )
    }

    override fun onClick(): Boolean
    {
        super.onClick()

        val recentlyClickedAmounts = Shop.getRecentlyClickedAmounts(player.name.string).filterNot {
            it.key is LeaveShopEntry || it.key is EmptyShopEntry
        }

        recentlyClickedAmounts.forEach { (shopEntry, count) ->
            // TODO
        }

        val recentlyBought = Shop.getRecentlyBought(player.name.string).toMutableList()

        // refunds of bought items cancel each other out
        recentlyBought.removeAll(recentlyBought.toSet().filterIsInstance<RefundShopEntry>().map { it.shopEntryToRefund })
        recentlyBought.removeAll(recentlyBought.toSet().filterIsInstance<RefundShopEntry>())

        // leaving shop cannot be refunded
        recentlyBought.removeAll(recentlyBought.toSet().filterIsInstance<LeaveShopEntry>())

        val upgradesInRecentlyBought = recentlyBought.toList().filterIsInstance<UpgradeableShopEntry>()

        upgradesInRecentlyBought.map { it.type }

        upgradesInRecentlyBought.distinctBy { it.type }.forEach { distinctShopEntry ->
            val diff = upgradesInRecentlyBought.count { distinctShopEntry.type == it.type }
            val cost = distinctShopEntry.addLevel(-diff) // - because we want to refund
            player.deductDGMoney(cost) // cost is already negative, as refunding is negative cost
        }

        recentlyBought.toList().filterNot { it is UpgradeableShopEntry }.forEach {
            if (it.hasGoods())
            {
                val price = -it.getTotalSpentMoney()
                player.deductDGMoney(price) // always refund 100% if recent refund
                it.removeGoods()
                StatManager.addRecentlyRefunded(player.name.string, it, price)
            }
        }

        Shop.clearRecentlyBought(player.name.string)

        return true
    }

    override fun hasGoods(): Boolean = false // this ShopEntry is not refundable
    override fun removeGoods() = Unit // refund should do nothing
}