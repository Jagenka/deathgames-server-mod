package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.gameplay.graplinghook.BlackjackAndHookers
import de.jagenka.itemAndNbtEqual
import de.jagenka.managers.MoneyManager
import de.jagenka.managers.getDGMoney
import de.jagenka.setCustomName
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.NbtComponent
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
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
        val nbt = NbtCompound()
        nbt.putDouble("hookMaxDistance", maxDistance)
        nbt.putInt("hookCooldown", cooldown)
        itemStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt))

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
        return attemptSale(player, price) {
            player.giveItemStack(itemStack.copy())
        }
    }

    override fun hasGoods(): Boolean
    {
        return player.inventory.contains(itemStack)
    }

    override fun removeGoods()
    {
        val filter: (ItemStack) -> Boolean = {
            itemAndNbtEqual(itemStack, it)
        }

        player.inventory.remove(filter, 1, player.inventory)
    }
}