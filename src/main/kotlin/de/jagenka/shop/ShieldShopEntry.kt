package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.managers.MoneyManager
import de.jagenka.managers.getDGMoney
import de.jagenka.maxDamage
import de.jagenka.setCustomName
import de.jagenka.withDamage
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

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
        Items.SHIELD.defaultInstance.copy().setCustomName(
            Component.literal("${MoneyManager.getCurrencyString(getPrice())}: $name x1").withColor(
                if (getDGMoney(playerName) < price) Util.getRGBInt(123, 0, 0)
                else Util.getRGBInt(255, 255, 255)
            )
        )

    override fun onClick(): Boolean
    {
        return attemptSale(player, price) {
            // first, look in offhand for an upgradable shield
            val upgradableShield = (player?.inventory?.equipment?.get(EquipmentSlot.OFFHAND)?.takeIf { itemStackInOffhand ->
                itemStackInOffhand.item == Items.SHIELD && itemStackInOffhand.damageValue >= targetDurability
            }
            // if not present, look in main inventory
                ?: player?.inventory?.items?.find { itemStackInInv ->
                    itemStackInInv.item == Items.SHIELD && itemStackInInv.damageValue >= targetDurability
                })
            if (upgradableShield != null)
            {
                upgradableShield.damageValue -= targetDurability
            } else
            {
                player?.addItem(ItemStack(Items.SHIELD).withDamage(Items.SHIELD.maxDamage - targetDurability).copy())
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
        shield.damageValue += targetDurability
        if (shield.damageValue == shield.maxDamage)
        {
            player?.inventory?.clearOrCountMatchingItems(
                { it == shield },
                1,
                player!!.inventory
            ) // should be null-safe, because remove will not be called, if player is null
        }
    }

    private fun getShieldForRefund(): ItemStack?
    {
        return player?.inventory?.items?.find { itemStackInInv ->
            itemStackInInv.item == Items.SHIELD && itemStackInInv.damageValue <= Items.SHIELD.maxDamage - targetDurability
        }
            ?: player?.inventory?.equipment?.get(EquipmentSlot.OFFHAND)?.takeIf { itemStackInInv ->
                itemStackInInv.item == Items.SHIELD && itemStackInInv.damageValue <= Items.SHIELD.maxDamage - targetDurability
            } // if item is in offhand
    }
}