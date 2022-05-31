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
        val upgradeLevel = getUpgradeLevel(player)
        if (upgradeLevel !in -1 until prices.size - 1) return false
        val nextLevel = upgradeLevel + 1

        if (player.getDGMoney() >= prices[nextLevel])
        {
            if (nextLevel > 0) boughtItemStacks[nextLevel - 1].forEach {
                player.inventory.remove({ oldItemStack ->
                    (oldItemStack.item !is ArmorItem) && (oldItemStack.item == it.item)
                }, 0, player.inventory)
            }
            boughtItemStacks[nextLevel].forEach { itemStack ->
                if (itemStack.item is ArmorItem)
                {
                    player.inventory.armor[when ((itemStack.item as ArmorItem).slotType)
                    {
                        EquipmentSlot.HEAD -> 3
                        EquipmentSlot.CHEST -> 2
                        EquipmentSlot.LEGS -> 1
                        EquipmentSlot.FEET -> 0
                        else -> return@forEach
                    }] = itemStack
                } else
                {
                    player.giveItemStack(itemStack)
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
