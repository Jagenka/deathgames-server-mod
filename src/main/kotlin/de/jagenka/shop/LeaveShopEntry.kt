package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.timer.ShopTask
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text

class LeaveShopEntry : ShopEntry
{
    override fun getPrice(player: ServerPlayerEntity): Int = 0

    override fun getDisplayItemStack(player: ServerPlayerEntity): ItemStack
    {
        return Items.BARRIER.defaultStack.copy()
            .setCustomName(
                Text.of("Leave Shop").getWithStyle(
                    Style.EMPTY.withColor(
                        Util.getTextColor(255, 255, 255)
                    )
                )[0]
            )
    }

    override fun buy(player: ServerPlayerEntity): Boolean
    {
        ShopTask.exitShop(player)
        return true
    }

    override val nameForStat: String
        get() = "LEAVE_SHOP"
}