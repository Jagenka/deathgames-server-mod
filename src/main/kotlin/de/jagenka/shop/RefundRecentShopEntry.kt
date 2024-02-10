package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.managers.refundMoney
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
        val recentlyClickedAmounts = Shop.getRecentlyClickedAmounts(player.name.string)
            .filterNot {
                it.key is LeaveShopEntry || it.key is EmptyShopEntry || it.key is RefundRecentShopEntry || it.key is RefundShopEntry
            }
            .filter { it.value > 0 }

        recentlyClickedAmounts.forEach { (shopEntry, count) ->
            if (shopEntry is UpgradeableShopEntry)
            {
                val moneySpent = shopEntry.addLevel(-count)
                player.refundMoney(moneySpent)
                StatManager.addRecentlyRefunded(player.name.string, shopEntry, moneySpent)
            } else
            {
                repeat(count) {
                    if (shopEntry.hasGoods())
                    {
                        val moneySpent = shopEntry.getTotalSpentMoney()
                        player.refundMoney(moneySpent)
                        shopEntry.removeGoods()
                        StatManager.addRecentlyRefunded(player.name.string, shopEntry, moneySpent)
                    }
                }
            }
        }

        Shop.clearRecentlyBought(player.name.string)

        return true
    }

    override fun hasGoods(): Boolean = false // this ShopEntry is not refundable
    override fun removeGoods() = Unit // refund should do nothing
}