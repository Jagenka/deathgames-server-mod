package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.managers.MoneyManager
import de.jagenka.managers.getDGMoney
import de.jagenka.maxDamage
import de.jagenka.setCustomName
import de.jagenka.withDamage
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ItemStack
import net.minecraft.item.Items.SHIELD
import net.minecraft.text.Style
import net.minecraft.text.Text

class ShieldShopEntry(playerName: String, private val name: String = "Shield", private val targetDurability: Int = 120, private val price: Int = 50) :
    ShopEntry(playerName, nameForStat = name)
{
    /**
     * count refers to durability bought in this case
     */
    override val amount: Int
        get() = targetDurability

    override fun getPrice(): Int = price

    override fun getDisplayItemStack(): ItemStack =
        SHIELD.defaultStack.copy().setCustomName(
            Text.of("${MoneyManager.getCurrencyString(getPrice())}: $name x1").getWithStyle(
                Style.EMPTY.withColor(
                    if (getDGMoney(playerName) < getPrice()) Util.getTextColor(123, 0, 0)
                    else Util.getTextColor(255, 255, 255)
                )
            )[0]
        )

    override fun onClick(): Boolean
    {
        return attemptSale(player, price) {
            // first, look in offhand for an upgradable shield
            val upgradableShield = (player?.inventory?.equipment?.get(EquipmentSlot.OFFHAND)?.takeIf { itemStackInOffhand ->
                itemStackInOffhand.item == SHIELD && itemStackInOffhand.damage >= targetDurability
            }
            // if not present, look in main inventory
                ?: player?.inventory?.main?.find { itemStackInInv ->
                    itemStackInInv.item == SHIELD && itemStackInInv.damage >= targetDurability
                })
            if (upgradableShield != null)
            {
                upgradableShield.damage -= targetDurability
            } else
            {
                player?.giveItemStack(ItemStack(SHIELD).withDamage(SHIELD.maxDamage - targetDurability).copy())
            }
        }
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
            player?.inventory?.remove({ it == shield }, 1, player!!.inventory) // should be null-safe, because remove will not be called, if player is null
        }
    }

    private fun getShieldForRefund(): ItemStack?
    {
        return player?.inventory?.main?.find { itemStackInInv ->
            itemStackInInv.item == SHIELD && itemStackInInv.damage <= SHIELD.maxDamage - targetDurability
        }
            ?: player?.inventory?.equipment?.get(EquipmentSlot.OFFHAND)?.takeIf { itemStackInInv ->
                itemStackInInv.item == SHIELD && itemStackInInv.damage <= SHIELD.maxDamage - targetDurability
            } // if item is in offhand
    }
}