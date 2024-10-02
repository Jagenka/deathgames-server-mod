package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.managers.KillManager
import de.jagenka.managers.MoneyManager.getCurrencyString
import de.jagenka.managers.getDGMoney
import de.jagenka.setCustomName
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text

class ExtraLifeShopEntry(player: ServerPlayerEntity, private val displayItemStack: ItemStack, private val price: Int, override var displayName: String) :
    ShopEntry(player = player, nameForStat = "EXTRA_LIFE")
{
    override fun getPrice(): Int = price

    override fun getDisplayItemStack(): ItemStack = displayItemStack.copy().setCustomName(
        Text.of("${getCurrencyString(price)}: $displayName").getWithStyle(
            Style.EMPTY.withColor(
                if (player.getDGMoney() < price) Util.getTextColor(123, 0, 0)
                else Util.getTextColor(255, 255, 255)
            )
        )[0]
    )

    override fun onClick(): Boolean
    {
        return attemptSale(player, price)
        {
            KillManager.addLives(player.name.string, 1)
        }
    }

    override fun hasGoods(): Boolean
    {
        return (KillManager.getRespawns(player) ?: 0) >= 1
    }

    override fun removeGoods()
    {
        KillManager.removeOneRespawn(player)
    }
}