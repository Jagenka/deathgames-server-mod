package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.managers.DisplayManager.sendPrivateMessage
import de.jagenka.setCustomName
import de.jagenka.timer.ShopTask
import de.jagenka.timer.Timer
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Style
import net.minecraft.text.Text

class LeaveShopEntry(playerName: String) : ShopEntry(playerName, nameForStat = "LEAVE_SHOP")
{
    override fun getPrice(): Int = 0

    override fun getDisplayItemStack(): ItemStack
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