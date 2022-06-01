package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.Util.sendPrivateMessage
import de.jagenka.deductDGMoney
import de.jagenka.getDGMoney
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.registry.Registry

class UpgradeableShopEntry(
    private val type: UpgradeType,
    private val boughtItemStacks: MutableList<MutableList<ItemStack>>,
    private val prices: MutableList<Int>,
    private val name: String
) : ShopEntry
{
    init
    {
        boughtItemStacks.forEach { if (it.isEmpty()) it.add(ItemStack.EMPTY) } // make sure there is a first item to be shown
        if (boughtItemStacks.size < prices.size) // make both lists to be the same size
        {
            repeat(prices.size - boughtItemStacks.size) { boughtItemStacks.add(mutableListOf(ItemStack.EMPTY)) }
        } else if (boughtItemStacks.size > prices.size)
        {
            repeat(boughtItemStacks.size - prices.size) { prices.add(69_420) }
        }
    }

    override fun getPrice(player: ServerPlayerEntity): Int = prices[Shop.getUpgradeLevel(player, type)]

    override fun getDisplayItemStack(player: ServerPlayerEntity): ItemStack
    {
        val nextLevel = getUpgradeLevel(player) + 1
        if (nextLevel >= prices.size) return ItemStack.EMPTY
        val price = prices[nextLevel]
        return boughtItemStacks[Shop.getUpgradeLevel(player, type)][0].copy().setCustomName(
            Text.of("${Shop.SHOP_UNIT}$price: $name").getWithStyle(
                Style.EMPTY.withColor(
                    if (player.getDGMoney() < price) Util.getTextColor(123, 0, 0)
                    else Util.getTextColor(255, 255, 255)
                )
            )[0]
        )
    } // first item in list is shown

    override fun buy(player: ServerPlayerEntity): Boolean
    {
        val currentLevel = getUpgradeLevel(player)
        if (currentLevel !in -1 until prices.size - 1) return false
        val nextLevel = currentLevel + 1

        if (player.getDGMoney() >= prices[nextLevel])
        {
            if (currentLevel >= 0) boughtItemStacks[currentLevel].forEach { itemStackToRemove ->
                player.inventory.remove({ itemStackInInventory ->
                    ((itemStackInInventory.item !is ArmorItem) && (itemStackInInventory.item == itemStackToRemove.item))
                }, -1, player.playerScreenHandler.craftingInput)
            }

            boughtItemStacks[nextLevel].forEach { itemStack ->
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
                            player.inventory.armor[index] = itemStack.copy()
                        }
                    }
                } else
                {
                    player.giveItemStack(itemStack.copy()) // pls end me
                }
            }

            player.deductDGMoney(prices[nextLevel])
            Shop.increaseUpgradeLevel(player, type)
            return true
        } else
        {
            player.sendPrivateMessage("You do not have the required ${Shop.SHOP_UNIT}${prices[nextLevel]}")
        }
        return false
    }

    private fun getUpgradeLevel(player: ServerPlayerEntity) = Shop.getUpgradeLevel(player, type) - 1
}
