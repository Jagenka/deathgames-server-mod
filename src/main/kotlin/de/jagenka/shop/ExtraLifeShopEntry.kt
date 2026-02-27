package de.jagenka.shop

import de.jagenka.managers.KillManager
import de.jagenka.managers.MoneyManager.getCurrencyString
import de.jagenka.setCustomName
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

class ExtraLifeShopEntry(playerName: String, private val displayItemStack: ItemStack, private val price: Int, override var displayName: String) :
    ShopEntry(playerName, nameForStat = "EXTRA_LIFE")
{
    override fun getPrice(): Int = price

    override fun getDisplayItemStack(): ItemStack = displayItemStack.copy().setCustomName(
        Component.literal("${getCurrencyString(price)}: $displayName").coloredForShop()
    )

    override fun onClick(): Boolean
    {
        return attemptSale(player, price)
        {
            KillManager.addLives(playerName, 1)
        }
    }

    override fun hasGoods(): Boolean
    {
        return (KillManager.getRespawns(playerName) ?: 0) >= 1
    }

    override fun removeGoods()
    {
        KillManager.removeOneRespawn(playerName)
    }
}