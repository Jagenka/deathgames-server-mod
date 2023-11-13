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

class ShieldShopEntry(player: ServerPlayerEntity, private val name: String = "Shield", private val targetDurability: Int = 120, private val price: Int = 50) :
    ShopEntry(player = player, nameForStat = name)
{
    override fun getPrice(): Int = price

    override fun getDisplayItemStack(): ItemStack =
        SHIELD.defaultStack.copy().setCustomName(
            Text.of("${MoneyManager.getCurrencyString(getPrice())}: $name x1").getWithStyle(
                Style.EMPTY.withColor(
                    if (player.getDGMoney() < getPrice()) Util.getTextColor(123, 0, 0)
                    else Util.getTextColor(255, 255, 255)
                )
            )[0]
        )

    override fun onClick(): Boolean
    {
        super.onClick()

        if (player.getDGMoney() >= getPrice())
        {
            val upgradableShield = player.inventory.main.find { itemStackInInv ->
                itemStackInInv.item == SHIELD && itemStackInInv.damage >= targetDurability
            }
                ?: player.inventory.offHand.find { itemStackInInv ->
                    itemStackInInv.item == SHIELD && itemStackInInv.damage >= targetDurability
                } // if item is in offhand
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

    override fun hasGoods(): Boolean
    {
        return getShieldForRefund() != null
    }

    override fun removeGoods()
    {
        val shield = getShieldForRefund() ?: return // this should not happen, as hasItem should have found a shield
        shield.damage += targetDurability
        if (shield.damage == shield.maxDamage)
        {
            player.inventory.remove({ it == shield }, 1, player.inventory)
        }
    }

    private fun getShieldForRefund(): ItemStack?
    {
        return player.inventory.main.find { itemStackInInv ->
            itemStackInInv.item == SHIELD && itemStackInInv.damage <= SHIELD.maxDamage - targetDurability
        }
            ?: player.inventory.offHand.find { itemStackInInv ->
                itemStackInInv.item == SHIELD && itemStackInInv.damage <= SHIELD.maxDamage - targetDurability
            } // if item is in offhand
    }
}