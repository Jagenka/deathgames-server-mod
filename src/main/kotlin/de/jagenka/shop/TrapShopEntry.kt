package de.jagenka.shop

import de.jagenka.Util
import de.jagenka.managers.DisplayManager.sendPrivateMessage
import de.jagenka.managers.MoneyManager
import de.jagenka.managers.deductDGMoney
import de.jagenka.managers.getDGMoney
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text

class TrapShopEntry(
    private val name: String,
    private val price: Int,
    isSnare: Boolean,
    effects: List<NbtCompound>,
    triggerRange: Double,
    setupTime: Int,
    triggerVisibilityRange: Double,
    visibilityRange: Double,
    affectedRange: Double,
    triggerDuration: Int
) : ShopEntry
{
    private val itemStack = ItemStack(Items.BAT_SPAWN_EGG)

    init
    {
        val effectsNbt = NbtList()
        effectsNbt.addAll(effects)
        itemStack.orCreateNbt.put("trapEffects", effectsNbt)
        itemStack.orCreateNbt.putBoolean("isSnareTrap", isSnare)
        itemStack.orCreateNbt.putDouble("trapTriggerRange", triggerRange)
        itemStack.orCreateNbt.putInt("trapSetupTime", setupTime)
        itemStack.orCreateNbt.putDouble("trapTriggerVisibilityRange", triggerVisibilityRange)
        itemStack.orCreateNbt.putDouble("trapVisibilityRange", visibilityRange)
        itemStack.orCreateNbt.putDouble("trapAffectedRange", affectedRange)
        itemStack.orCreateNbt.putInt("trapTriggerDuration", triggerDuration)
    }

    override val nameForStat: String
        get() = "${name}_trap"

    override fun getPrice(player: ServerPlayerEntity): Int = price

    override fun getDisplayItemStack(player: ServerPlayerEntity): ItemStack =
        Items.BAT_SPAWN_EGG.defaultStack.setCustomName(
            Text.of("${MoneyManager.getCurrencyString(price)}: $name x1").getWithStyle(
                Style.EMPTY.withColor(
                    if (player.getDGMoney() < price) Util.getTextColor(123, 0, 0)
                    else Util.getTextColor(255, 255, 255)
                )
            )[0]
        )

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