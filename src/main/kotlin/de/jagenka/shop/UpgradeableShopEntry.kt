package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.managers.DisplayManager.sendPrivateMessage
import de.jagenka.managers.MoneyManager
import de.jagenka.managers.deductDGMoney
import de.jagenka.managers.getDGMoney
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text

class UpgradeableShopEntry(
    val type: String,
    boughtItemStacks: MutableList<MutableList<ItemStack>>,
    prices: MutableList<Int>,
    private val name: String
) : ShopEntry
{
    private val prices: PriceList
    private val items: ItemsList

    init
    {
        val pricesCopy = prices.toMutableList()
        val itemsCopy = boughtItemStacks.toMutableList()

        itemsCopy.forEach { if (it.isEmpty()) it.add(ItemStack.EMPTY) } // make sure there is a first item to be shown
        if (itemsCopy.size < pricesCopy.size) // make both lists to be the same size
        {
            repeat(pricesCopy.size - itemsCopy.size) { itemsCopy.add(mutableListOf(ItemStack.EMPTY)) }
        } else if (itemsCopy.size > pricesCopy.size)
        {
            repeat(itemsCopy.size - pricesCopy.size) { pricesCopy.add(69_420) }
        }

        this.prices = PriceList(pricesCopy)
        this.items = ItemsList(itemsCopy)
    }

    // this gets the price for next level
    override fun getPrice(player: ServerPlayerEntity): Int
    {
        val targetLevel = Shop.getUpgradableLevel(player.name.string, type)
        return prices[targetLevel]
    }

    override fun getDisplayItemStack(player: ServerPlayerEntity): ItemStack
    {
        val nextLevel = getCurrentLevel(player) + 1
        return getDisplayItemStackForLevel(player, nextLevel)

    }

    fun getPreviousDisplayItemStack(player: ServerPlayerEntity): ItemStack
    {
        return getDisplayItemStackForLevel(player, getCurrentLevel(player))
    }

    /**
     *  first item in boughtItemStacks list is shown
     */
    private fun getDisplayItemStackForLevel(player: ServerPlayerEntity, level: Int): ItemStack
    {
        if (level !in prices) return ItemStack.EMPTY
        val price = prices[level]
        return items[level].getOrElse(0) { ItemStack.EMPTY }.copy().setCustomName( // - 1, as level 0 is not bought
            Text.of("${MoneyManager.getCurrencyString(price)}: $name").getWithStyle(
                Style.EMPTY.withColor(
                    if (player.getDGMoney() < price) Util.getTextColor(123, 0, 0)
                    else Util.getTextColor(255, 255, 255)
                )
            )[0]
        )
    }

    override fun buy(player: ServerPlayerEntity): Boolean
    {
        val currentLevel = getCurrentLevel(player)
        if (!prices.canUpgrade(currentLevel)) return false

        val targetLevel = currentLevel + 1

        if (player.getDGMoney() >= prices[targetLevel])
        {
            val cost = setToLevel(player, targetLevel)
            player.deductDGMoney(cost)
            return true
        } else
        {
            player.sendPrivateMessage(Shop.getNotEnoughMoneyString(prices[targetLevel]))
        }
        return false
    }

    /**
     * @return how much this cost
     */
    private fun setToLevel(player: ServerPlayerEntity, targetLevel: Int): Int
    {
        val currentLevel = getCurrentLevel(player)

        if (targetLevel == currentLevel) return 0

        // remove items currently equipped
        if (currentLevel >= 0) items[currentLevel].forEach { itemStackToRemove -> // - 1, as level 0 is not bought
            player.inventory.remove({ itemStackInInventory ->
                ((itemStackInInventory.item !is ArmorItem) && (itemStackInInventory.item == itemStackToRemove.item))
            }, -1, player.playerScreenHandler.craftingInput)
        }

        items[targetLevel].forEach { itemStack -> // - 1, as level 0 is not bought
            // remove only the items in slots that need to be filled with new armor
            if (itemStack.item is ArmorItem)
            {
                listOf(EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD).forEachIndexed { index, equipmentSlot ->
                    if ((itemStack.item as? ArmorItem)?.slotType == equipmentSlot)
                    {
                        player.inventory.remove(
                            { ((it.item as? ArmorItem)?.slotType == equipmentSlot) },
                            -1,
                            player.playerScreenHandler.craftingInput
                        )
                        // and also put the new armor into the slot
                        player.inventory.armor[index] = itemStack.copy()
                    }
                }
            } else
            {
                // otherwise just give the new item, as the old one has been removed before
                player.giveItemStack(itemStack.copy())
            }
        }

        Shop.setUpgradableLevel(player.name.string, type, targetLevel)

        return (if (targetLevel < currentLevel) -1 else 1) * // negative cost if downgrade
                prices.sumBetween(currentLevel, targetLevel)
    }

    private fun getCurrentLevel(player: ServerPlayerEntity): Int = Shop.getUpgradableLevel(player.name.string, type)

    override fun getTotalSpentMoney(player: ServerPlayerEntity): Int
    {
        val currentLevel = getCurrentLevel(player)
        return prices.sumBetween(currentLevel, 0)
    }

    override fun getDisplayName(): String = name

    override fun hasItem(player: ServerPlayerEntity): Boolean
    {
        return getCurrentLevel(player) > 0
    }

    override fun removeItem(player: ServerPlayerEntity)
    {
        setToLevel(player, 0)
    }

    override val nameForStat: String
        get() = "${type}_UPGRADE"

    override fun toString(): String
    {
        return "$name $type ${
            items.joinToString(separator = ", ", prefix = "[", postfix = "]") { lvls ->
                lvls.joinToString(separator = ", ", prefix = "[", postfix = "]") {
                    "$it ${it.nbt}"
                }
            }
        } $prices"
    }
}
