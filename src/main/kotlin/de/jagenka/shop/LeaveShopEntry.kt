package de.jagenka.shop

import de.jagenka.managers.DisplayManager.sendPrivateMessage
import de.jagenka.setCustomName
import de.jagenka.timer.ShopTask
import de.jagenka.timer.Timer
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class LeaveShopEntry(playerName: String) : ShopEntry(playerName, nameForStat = "LEAVE_SHOP")
{
    override fun getPrice(): Int = 0

    override fun getDisplayItemStack(): ItemStack
    {
        return Items.BARRIER.defaultInstance.copy()
            .setCustomName(
                Component.literal("Leave Shop").coloredForShop()
            )
    }

    override fun onClick(): Boolean
    {
        if (Timer.gameMechsPaused)
        {
            player?.sendPrivateMessage("Cannot leave right now!")
            return false
        }
        ShopTask.exitShop(playerName)
        return true
    }

    override fun hasGoods(): Boolean = false // this ShopEntry is not refundable
    override fun removeGoods() = Unit // refund should do nothing
}