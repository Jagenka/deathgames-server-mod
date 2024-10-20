package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.managers.MoneyManager
import de.jagenka.managers.getDGMoney
import de.jagenka.setCustomName
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.text.Style
import net.minecraft.text.Text
import kotlin.math.max
import kotlin.math.min

class UpgradeableShopEntry(
    playerName: String,
    val type: String,
    private val items: MutableList<MutableList<ItemStack>>,
    private val prices: MutableList<Int>,
    private val name: String
) : ShopEntry(playerName, nameForStat = "${type}_UPGRADE")
{
    private val armorSlots = listOf(EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD)

    init
    {
        items.forEach { if (it.isEmpty()) it.add(ItemStack.EMPTY) } // make sure there is a first item to be shown
        if (items.size < prices.size) // make both lists to be the same size
        {
            repeat(prices.size - items.size) { items.add(mutableListOf(ItemStack.EMPTY)) }
        } else if (items.size > prices.size)
        {
            repeat(items.size - prices.size) { prices.add(69_420) }
        }
    }

    /**
     * get price for next level
     */
    override fun getPrice(): Int
    {
        val nextLevel = getCurrentLevel() + 1
        if (nextLevel !in prices.indices) return 0
        return prices[nextLevel]
    }

    /**
     * get display ItemStack for next level
     */
    override fun getDisplayItemStack(): ItemStack
    {
        val nextLevel = getCurrentLevel() + 1
        return getDisplayItemStackForLevel(nextLevel)

    }

    /**
     * get display ItemStack for current level (for refund)
     */
    fun getCurrentLevelDisplayItemStack(): ItemStack
    {
        return getDisplayItemStackForLevel(getCurrentLevel())
    }

    /**
     * first item in items list is shown
     */
    private fun getDisplayItemStackForLevel(level: Int): ItemStack
    {
        if (level !in prices.indices) return ItemStack.EMPTY
        val price = prices[level]
        return items[level].getOrElse(0) { ItemStack.EMPTY }.copy().setCustomName(
            Text.of("${MoneyManager.getCurrencyString(price)}: $name").getWithStyle(
                Style.EMPTY.withColor(
                    if (getDGMoney(playerName) < price) Util.getTextColor(123, 0, 0)
                    else Util.getTextColor(255, 255, 255)
                )
            )[0]
        )
    }

    override fun onClick(): Boolean
    {
        val targetLevel = getCurrentLevel() + 1

        if (targetLevel !in prices.indices) return false

        return attemptSale(player, prices[targetLevel]) {
            setToLevel(targetLevel)
        }
    }

    /**
     * adds (or subtracts if negative) level to(/from) player's upgrade
     * @return how much this cost
     */
    fun addLevel(diff: Int): Int
    {
        val currentLevel = getCurrentLevel()
        val targetLevel = currentLevel + diff

        return setToLevel(targetLevel)
    }

    /**
     * sets upgrade level and manages item removal and giving
     * @param targetLevel what level to set to (-1 is no level)
     * @return how much this cost
     */
    private fun setToLevel(targetLevel: Int): Int
    {
        val currentLevel = getCurrentLevel()

        if (targetLevel == currentLevel) return 0
        if (targetLevel >= prices.size) return 0

        // remove currently equipped items - can only work if current level is in range
        if (currentLevel in items.indices)
        {
            if (targetLevel !in items.indices)
            {
                // remove all items from all upgrade levels when downgrading to level -1
                items.flatMap { it }.forEach { itemStackToRemove ->
                    player?.inventory?.remove({ itemStackInInventory ->
                        itemStackInInventory.item == itemStackToRemove.item
                    }, -1, player!!.playerScreenHandler.craftingInput)
                }
            } else
            {
                items[currentLevel].forEach { itemStackToRemove ->
                    player?.inventory?.remove({ itemStackInInventory ->
                        (itemStackInInventory.item == itemStackToRemove.item) &&
                                (itemStackInInventory.item !is ArmorItem) // do not remove armor items, as they get replaced instead later
                    }, -1, player!!.playerScreenHandler.craftingInput) // should be null-safe, because remove will not be called, if player is null
                }
            }
        }

        // if target level is another upgrade level, give them the stuffs
        if (targetLevel in items.indices)
        {
            items[targetLevel].forEach { itemStack ->
                // remove only the items in slots that need to be filled with new armor
                if (itemStack.item is ArmorItem)
                {
                    armorSlots.forEachIndexed { index, equipmentSlot ->
                        if ((itemStack.item as? ArmorItem)?.slotType == equipmentSlot)
                        {
                            player?.inventory?.remove(
                                { ((it.item as? ArmorItem)?.slotType == equipmentSlot) },
                                -1,
                                player!!.playerScreenHandler.craftingInput // should be null-safe, because remove will not be called, if player is null
                            )
                            // and also put the new armor into the slot
                            player?.inventory?.armor[index] = itemStack.copy()
                        }
                    }
                } else
                {
                    // otherwise just give the new item, as the old one has been removed before
                    player?.giveItemStack(itemStack.copy())
                }
            }
        }


        Shop.setLevelForUpgradeType(playerName, type, targetLevel)

        return (if (targetLevel < currentLevel) -1 else 1) * // negative cost if downgrade
                priceSumBetween(currentLevel, targetLevel)
    }

    private fun priceSumBetween(currentLevel: Int, targetLevel: Int): Int
    {
        return (min(currentLevel, targetLevel) + 1).rangeUntil(max(currentLevel, targetLevel) + 1) // range from lower to higher value, else range is empty
            .toList().sumOf { prices[it] } // sum up individual level costs
    }

    private fun getCurrentLevel(): Int = Shop.getLevelForUpgradeType(playerName, type)

    override fun getTotalSpentMoney(): Int
    {
        val currentLevel = getCurrentLevel()
        return priceSumBetween(currentLevel, -1)
    }

    override var displayName: String = name

    override fun hasGoods(): Boolean
    {
        return getCurrentLevel() >= 0
    }

    override fun removeGoods()
    {
        setToLevel(-1)
    }

    override fun toString(): String
    {
        return "$name $type ${
            items.joinToString(separator = ", ", prefix = "[", postfix = "]") { lvls ->
                lvls.joinToString(separator = ", ", prefix = "[", postfix = "]") {
                    "$it ${it.components}"
                }
            }
        } $prices"
    }
}
