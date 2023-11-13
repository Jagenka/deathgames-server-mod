package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.gameplay.graplinghook.BlackjackAndHookers
import de.jagenka.managers.DisplayManager.sendPrivateMessage
import de.jagenka.managers.MoneyManager
import de.jagenka.managers.deductDGMoney
import de.jagenka.managers.getDGMoney
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text

class HookerShopEntry(
    player: ServerPlayerEntity,
    override var displayName: String = "Grappling Hook", private val price: Int = 0,
    maxDistance: Double,
    cooldown: Int
) : ShopEntry(player = player, nameForStat = displayName)
{
    private val itemStack = ItemStack(BlackjackAndHookers.itemItem)

    init
    {
        itemStack.orCreateNbt.putDouble("hookMaxDistance", maxDistance)
        itemStack.orCreateNbt.putInt("hookCooldown", cooldown)

        itemStack.setCustomName(Text.of(displayName).getWithStyle(Style.EMPTY.withItalic(false))[0])
    }

    override fun getPrice(): Int = price

    override fun getDisplayItemStack(): ItemStack
    {
        return BlackjackAndHookers.itemItem.defaultStack.copy().setCustomName(
            Text.of("${MoneyManager.getCurrencyString(price)}: $displayName x1").getWithStyle(
                Style.EMPTY.withColor(
                    if (player.getDGMoney() < price) Util.getTextColor(123, 0, 0)
                    else Util.getTextColor(255, 255, 255)
                )
            )[0]
        )
    }

    override fun onClick(): Boolean
    {
        super.onClick()

        if (player.getDGMoney() >= price)
        {
            player.giveItemStack(itemStack.copy())
            player.deductDGMoney(price)
            return true
        } else
        {
            player.sendPrivateMessage(Shop.getNotEnoughMoneyString(price))
        }
        return false
    }

    override fun hasGoods(): Boolean
    {
        return player.inventory.contains(itemStack)
    }

    override fun removeGoods()
    {
        val filter: (ItemStack) -> Boolean = { it.item == itemStack.item && it.nbt == itemStack.nbt }

        player.inventory.remove(filter, 1, player.inventory)
    }
}