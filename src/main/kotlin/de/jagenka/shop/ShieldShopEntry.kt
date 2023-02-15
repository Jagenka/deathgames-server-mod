package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.managers.DisplayManager.sendPrivateMessage
import de.jagenka.managers.MoneyManager
import de.jagenka.managers.deductDGMoney
import de.jagenka.managers.getDGMoney
import de.jagenka.shop.ShopEntries.withDamage
import net.minecraft.item.ItemStack
import net.minecraft.item.Items.SHIELD
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text

class ShieldShopEntry(private val targetDurability: Int = 120) : ShopEntry
{
    override val nameForStat: String
        get() = "SHIELD"

    private fun getPrice() = 50
    override fun getPrice(player: ServerPlayerEntity): Int = getPrice()

    override fun getDisplayItemStack(player: ServerPlayerEntity): ItemStack =
        SHIELD.defaultStack.copy().setCustomName(
            Text.of("${MoneyManager.getCurrencyString(getPrice())}: Shield x1").getWithStyle(
                Style.EMPTY.withColor(
                    if (player.getDGMoney() < getPrice()) Util.getTextColor(123, 0, 0)
                    else Util.getTextColor(255, 255, 255)
                )
            )[0]
        )

    override fun buy(player: ServerPlayerEntity): Boolean
    {
        if (player.getDGMoney() >= getPrice())
        {
            val upgradableShield = player.inventory.main.find { itemStackInInv ->
                if (itemStackInInv.item == SHIELD) println(itemStackInInv.damage)
                itemStackInInv.item == SHIELD && itemStackInInv.damage >= targetDurability
            }
            if (upgradableShield != null)
            {
                upgradableShield.damage -= targetDurability
            } else
            {
                player.giveItemStack(ItemStack(SHIELD).withDamage(SHIELD.maxDamage - targetDurability).copy())
            }
            player.deductDGMoney(getPrice())
            return true
        } else
        {
            player.sendPrivateMessage(Shop.getNotEnoughMoneyString(getPrice()))
        }
        return false
    }
}