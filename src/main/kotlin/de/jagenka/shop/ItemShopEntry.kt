package de.jagenka.shop

import de.jagenka.itemAndNbtEqual
import de.jagenka.managers.MoneyManager.getCurrencyString
import de.jagenka.setCustomName
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

class ItemShopEntry(playerName: String, private val boughtItemStack: ItemStack, private val price: Int, override var displayName: String) :
    ShopEntry(playerName = playerName, nameForStat = displayName)
{
    override val amount: Int
        get() = boughtItemStack.count

    override fun getPrice(): Int = price

    override fun getDisplayItemStack(): ItemStack
    {
        return boughtItemStack.copy()
            .setCustomName(
                Component.literal("${getCurrencyString(price)}: $displayName x${boughtItemStack.count}")
                    .coloredForShop()
            )
    }

    override fun onClick(): Boolean
    {
        return attemptSale(player, price) {
            player?.addItem(boughtItemStack.copy())
        }
    }

    override fun hasGoods(): Boolean
    {
        return player?.inventory?.contains {
            itemAndNbtEqual(boughtItemStack, it) &&
                    it.count >= boughtItemStack.count
        } == true
    }

    override fun removeGoods()
    {
        val amount = boughtItemStack.count

        player?.inventory?.clearOrCountMatchingItems(
            { itemStackInInventory ->
                itemAndNbtEqual(boughtItemStack, itemStackInInventory)
            },
            amount, player!!.inventoryMenu.craftSlots // also check in craft slots
            // should be null-safe, because remove will not be called, if player is null
        )
    }

    override fun toString(): String
    {
        return "$displayName $boughtItemStack ${boughtItemStack.components} $price"
    }
}