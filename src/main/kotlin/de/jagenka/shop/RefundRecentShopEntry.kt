package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.managers.refundMoney
import de.jagenka.setCustomName
import de.jagenka.stats.StatManager
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Style
import net.minecraft.text.Text

class RefundRecentShopEntry(playerName: String, override var displayName: String = "Refund recent purchases") : ShopEntry(playerName, nameForStat = "refund_recent")
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
        val recentlyClickedAmounts = Shop.getRecentlyClickedAmounts(playerName)
            .filterNot {
                it.key is LeaveShopEntry || it.key is EmptyShopEntry || it.key is RefundRecentShopEntry || it.key is RefundShopEntry
            }
            .filter { it.value > 0 }

        recentlyClickedAmounts.forEach { (shopEntry, count) ->
            if (shopEntry is UpgradeableShopEntry)
            {
                val moneySpent = shopEntry.addLevel(-count)
                refundMoney(playerName, moneySpent)
                StatManager.addRecentlyRefunded(playerName, shopEntry, moneySpent)
            } else
            {
                repeat(count) {
                    if (shopEntry.hasGoods())
                    {
                        val moneySpent = shopEntry.getTotalSpentMoney()
                        refundMoney(playerName, moneySpent)
                        shopEntry.removeGoods()
                        StatManager.addRecentlyRefunded(playerName, shopEntry, moneySpent)
                    }
                }
            }
        }

        Shop.clearRecentlyBought(playerName)

        return true
    }

    override fun hasGoods(): Boolean = false // this ShopEntry is not refundable
    override fun removeGoods() = Unit // refund should do nothing
}