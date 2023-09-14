package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.managers.DisplayManager.sendPrivateMessage
import de.jagenka.managers.KillManager
import de.jagenka.managers.MoneyManager.getCurrencyString
import de.jagenka.managers.deductDGMoney
import de.jagenka.managers.getDGMoney
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text

class ExtraLifeShopEntry(private val displayItemStack: ItemStack, private val price: Int, private val name: String) : ShopEntry
{
    override fun getPrice(player: ServerPlayerEntity): Int = price

    override fun getDisplayItemStack(player: ServerPlayerEntity): ItemStack = displayItemStack.copy().setCustomName(
        Text.of("${getCurrencyString(price)}: $name").getWithStyle(
            Style.EMPTY.withColor(
                if (player.getDGMoney() < price) Util.getTextColor(123, 0, 0)
                else Util.getTextColor(255, 255, 255)
            )
        )[0]
    )

    override fun onClick(player: ServerPlayerEntity): Boolean
    {
        if (player.getDGMoney() >= price)
        {
            KillManager.addLives(player.name.string, 1)
            player.deductDGMoney(price)
            return true
        } else
        {
            player.sendPrivateMessage(Shop.getNotEnoughMoneyString(price))
        }
        return false
    }

    override val nameForStat: String
        get() = "EXTRA_LIFE"

    override fun hasItem(player: ServerPlayerEntity): Boolean
    {
        return (KillManager.getRespawns(player) ?: 0) >= 1
    }

    override fun removeItem(player: ServerPlayerEntity)
    {
        KillManager.removeOneRespawn(player)
    }
}