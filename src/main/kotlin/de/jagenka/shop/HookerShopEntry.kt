package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.gameplay.graplinghook.BlackjackAndHookers
import de.jagenka.itemAndNbtEqual
import de.jagenka.managers.MoneyManager
import de.jagenka.managers.getDGMoney
import de.jagenka.setCustomName
import net.minecraft.core.component.DataComponents.CUSTOM_DATA
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData

class HookerShopEntry(
    playerName: String,
    override var displayName: String = "Grappling Hook", private val price: Int = 0,
    maxDistance: Double,
    cooldown: Int
) : ShopEntry(playerName, nameForStat = displayName)
{
    private val itemStack = ItemStack(BlackjackAndHookers.itemItem)

    init
    {
        val nbt = CompoundTag()
        nbt.putDouble("hookMaxDistance", maxDistance)
        nbt.putInt("hookCooldown", cooldown)
        itemStack.set(CUSTOM_DATA, CustomData.of(nbt))

        itemStack.setCustomName(Component.literal(displayName).withStyle(Style.EMPTY.withItalic(false)))
    }

    override fun getPrice(): Int = price

    override fun getDisplayItemStack(): ItemStack
    {
        return BlackjackAndHookers.itemItem.defaultInstance.copy().setCustomName(
            Component.literal("${MoneyManager.getCurrencyString(price)}: $displayName x1")
                .withColor( // TODO: factor out text coloring, as many items use the same colors
                    if (getDGMoney(playerName) < price) Util.getRGBInt(123, 0, 0)
                    else Util.getRGBInt(255, 255, 255)
                )
        )
    }

    override fun onClick(): Boolean
    {
        return attemptSale(player, price) {
            player?.addItem(itemStack.copy())
        }
    }

    override fun hasGoods(): Boolean
    {
        return player?.inventory?.contains(itemStack) == true
    }

    override fun removeGoods()
    {
        val filter: (ItemStack) -> Boolean = {
            itemAndNbtEqual(itemStack, it)
        }

        player?.inventory?.clearOrCountMatchingItems(
            filter,
            1,
            player!!.inventory
        ) // should be null-safe, as remove will not be called, if player is null
    }
}