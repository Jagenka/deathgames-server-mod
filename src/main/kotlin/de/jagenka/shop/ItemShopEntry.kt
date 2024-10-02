package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.itemAndNbtEqual
import de.jagenka.managers.MoneyManager.getCurrencyString
import de.jagenka.managers.getDGMoney
import de.jagenka.setCustomName
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text

class ItemShopEntry(player: ServerPlayerEntity, private val boughtItemStack: ItemStack, private val price: Int, override var displayName: String) :
    ShopEntry(player, "${boughtItemStack.count} $displayName")
{
    override fun getPrice(): Int = price

    override fun getDisplayItemStack(): ItemStack
    {
        return boughtItemStack.copy()
            .setCustomName(
                Text.of("${getCurrencyString(price)}: $displayName x${boughtItemStack.count}").getWithStyle(
                    Style.EMPTY.withColor(
                        if (player.getDGMoney() < price) Util.getTextColor(123, 0, 0)
                        else Util.getTextColor(255, 255, 255)
                    )
                )[0]
            )
    }

    override fun onClick(): Boolean
    {
        return attemptSale(player, price) {
            player.giveItemStack(boughtItemStack.copy())
        }
    }

    override fun hasGoods(): Boolean
    {
        return player.inventory.containsAny {
            itemAndNbtEqual(boughtItemStack, it) &&
                    it.count >= boughtItemStack.count
        }
    }

    override fun removeGoods()
    {
        val amount = boughtItemStack.count

        player.inventory.remove({ itemStackInInventory ->
            itemAndNbtEqual(boughtItemStack, itemStackInInventory)
        }, amount, player.playerScreenHandler.craftingInput)
    }

    override fun toString(): String
    {
        return "$displayName $boughtItemStack ${boughtItemStack.components} $price"
    }
}