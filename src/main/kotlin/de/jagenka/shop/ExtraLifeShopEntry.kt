package de.jagenka.shop

import de.jagenka.DGKillManager
import de.jagenka.Util
import de.jagenka.Util.sendPrivateMessage
import de.jagenka.deductDGMoney
import de.jagenka.getDGMoney
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text

class ExtraLifeShopEntry(private val displayItemStack: ItemStack, private val price: Int, private val name: String) : ShopEntry
{
    override fun getPrice(player: ServerPlayerEntity): Int = price

    override fun getDisplayItemStack(player: ServerPlayerEntity): ItemStack = displayItemStack.copy().setCustomName(
        Text.of("${Shop.SHOP_UNIT}$price: $name").getWithStyle(
            Style.EMPTY.withColor(
                if (player.getDGMoney() < price) Util.getTextColor(123, 0, 0)
                else Util.getTextColor(255, 255, 255)
            )
        )[0]
    )

    override fun buy(player: ServerPlayerEntity): Boolean
    {
        if (player.getDGMoney() >= price)
        {
            DGKillManager.addLives(player, 1)
            player.deductDGMoney(price)
            return true
        } else
        {
            player.sendPrivateMessage("You do not have the required ${Shop.SHOP_UNIT}$price")
        }
        return false
    }
}