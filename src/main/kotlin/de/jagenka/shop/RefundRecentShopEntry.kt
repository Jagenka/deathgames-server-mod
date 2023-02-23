package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.managers.deductDGMoney
import de.jagenka.stats.StatManager
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text

class RefundRecentShopEntry(private val displayName: String = "Refund recent purchases") : ShopEntry
{
    override val nameForStat: String
        get() = "refund_recent"

    override fun getPrice(player: ServerPlayerEntity): Int = 0

    override fun getDisplayItemStack(player: ServerPlayerEntity): ItemStack
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

    override fun buy(player: ServerPlayerEntity): Boolean
    {
        val recentlyBought = Shop.getRecentlyBought(player.name.string).toMutableList()
        recentlyBought.removeAll(recentlyBought.toSet().filterIsInstance<RefundShopEntry>().map { it.shopEntryToRefund })
        recentlyBought.removeAll(recentlyBought.toSet().filterIsInstance<RefundShopEntry>())
        recentlyBought.removeAll(recentlyBought.toSet().filterIsInstance<LeaveShopEntry>())

        var tmp = 0
        recentlyBought.toList().distinctBy {// filters duplicate Upgradables, but keeps every other ShopEntry
            if (it is UpgradeableShopEntry) it
            else tmp++// just need to be different values
        }.forEach {
            if (it.hasItem(player))
            {
                val price = -it.getTotalSpentMoney(player)
                player.deductDGMoney(price) // always refund 100% if recent refund
                it.removeItem(player)
                StatManager.addBoughtItem(player.name.string, it, price)
            }
        }

        Shop.clearRecentlyBought(player.name.string)

        return true
    }

    override fun hasItem(player: ServerPlayerEntity): Boolean = false // this ShopEntry is not refundable
    override fun removeItem(player: ServerPlayerEntity) = Unit // refund should do nothing
}