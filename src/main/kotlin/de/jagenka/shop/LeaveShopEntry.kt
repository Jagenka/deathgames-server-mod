package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.managers.DisplayManager.sendPrivateMessage
import de.jagenka.timer.ShopTask
import de.jagenka.timer.Timer
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
        if (Timer.gameMechsPaused)
        {
            player.sendPrivateMessage("Cannot leave right now!")
            return false
        }
        ShopTask.exitShop(player)
        return true
    }

    override fun hasItem(player: ServerPlayerEntity): Boolean = false // this ShopEntry is not refundable
    override fun removeItem(player: ServerPlayerEntity) = Unit // refund should do nothing

    override val nameForStat: String
        get() = "LEAVE_SHOP"
}