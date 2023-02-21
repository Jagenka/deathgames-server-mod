package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.managers.DisplayManager.sendPrivateMessage
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text

class RefundRecentShopEntry : ShopEntry
{
    override val nameForStat: String
        get() = "refund_recent"

    override fun getPrice(player: ServerPlayerEntity): Int = 0

    override fun getDisplayItemStack(player: ServerPlayerEntity): ItemStack
    {
        return Items.BUNDLE.defaultStack.copy()
            .setCustomName(
                Text.of("Refund recent purchases").getWithStyle(
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
        // TODO: Shield - remove durability -> refund shield allgemein
        // TODO: Extra Life
        // TODO: Item, Upgradable
        // TODO: Trap refund -> allgemein
        player.sendPrivateMessage(recentlyBought.map { it.nameForStat }.toString())
        return false
    }
}