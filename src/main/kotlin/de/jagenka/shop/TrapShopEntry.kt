package de.jagenka.shop

import de.jagenka.gameplay.traps.DGStatusEffect
import de.jagenka.managers.DisplayManager.sendPrivateMessage
import de.jagenka.managers.deductDGMoney
import de.jagenka.managers.getDGMoney
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.server.network.ServerPlayerEntity

class TrapShopEntry(private val name: String, private val price: Int, private val effects: Set<DGStatusEffect>) : ShopEntry
{
    private val itemStack = ItemStack(Items.BAT_SPAWN_EGG)

    init
    {
        val effectsNbt = NbtList()
        effectsNbt.addAll(effects.map { NbtString.of(it.name) })
        itemStack.orCreateNbt.put("trapEffects", effectsNbt)
    }

    override val nameForStat: String
        get() = "${name}_trap"

    override fun getPrice(player: ServerPlayerEntity): Int = price

    override fun getDisplayItemStack(player: ServerPlayerEntity): ItemStack = Items.BAT_SPAWN_EGG.defaultStack

    override fun buy(player: ServerPlayerEntity): Boolean
    {
        if (player.getDGMoney() >= price)
        {
            player.giveItemStack(itemStack.copy())
            player.deductDGMoney(price)
            return true
        } else
        {
            player.sendPrivateMessage(Shop.getNotEnoughMoneyString(price))
        }
        return false
    }
}