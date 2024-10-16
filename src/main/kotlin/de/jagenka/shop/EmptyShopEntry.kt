package de.jagenka.shop

import net.minecraft.item.ItemStack

/**
 * placeholder ShopEntry in case something goes wrong
 */
class EmptyShopEntry(playerName: String) : ShopEntry(playerName, nameForStat = "EMPTY")
{
    override fun getPrice(): Int
    {
        return 0
    }

    override fun hasGoods(): Boolean
    {
        return false
    }

    override fun removeGoods() = Unit

    override fun getDisplayItemStack(): ItemStack = ItemStack.EMPTY
}