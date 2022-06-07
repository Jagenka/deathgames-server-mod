package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.managers.DisplayManager.sendPrivateMessage
import de.jagenka.managers.deductDGMoney
import de.jagenka.managers.getDGMoney
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text

class ItemShopEntry(private val boughtItemStack: ItemStack, private val price: Int, val name: String) : ShopEntry
{
    override fun getPrice(player: ServerPlayerEntity): Int = price

    override fun getDisplayItemStack(player: ServerPlayerEntity): ItemStack
    {
        return boughtItemStack.copy()
            .setCustomName(
                Text.of("${Shop.SHOP_UNIT}$price: $name x${boughtItemStack.count}").getWithStyle(
                    Style.EMPTY.withColor(
                        if (player.getDGMoney() < price) Util.getTextColor(123, 0, 0)
                        else Util.getTextColor(255, 255, 255)
                    )
                )[0]
            )
    }

    override fun buy(player: ServerPlayerEntity): Boolean
    {
        if (player.getDGMoney() >= price)
        {
            player.giveItemStack(boughtItemStack.copy())
            player.deductDGMoney(price)
            return true
        } else
        {
            player.sendPrivateMessage("You do not have the required ${Shop.SHOP_UNIT}$price")
        }
        return false
    }

    override fun getTotalSpentMoney(player: ServerPlayerEntity): Int = getPrice(player)

    override fun getDisplayName(): String = name
    override fun hasItem(player: ServerPlayerEntity): Boolean
    {
        val amount = boughtItemStack.count

        return player.inventory.count(boughtItemStack.item) >= amount
    }

    override fun removeItem(player: ServerPlayerEntity)
    {
        val amount = boughtItemStack.count

        player.inventory.remove({ itemStackInInventory ->
            itemStackInInventory.item == boughtItemStack.item
        }, amount, player.playerScreenHandler.craftingInput)
    }
}