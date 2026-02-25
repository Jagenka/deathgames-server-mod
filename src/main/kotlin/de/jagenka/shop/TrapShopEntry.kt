package de.jagenka.shop

import com.mojang.brigadier.StringReader
import de.jagenka.Util
import de.jagenka.itemAndNbtEqual
import de.jagenka.managers.MoneyManager
import de.jagenka.managers.getDGMoney
import de.jagenka.setCustomName
import net.minecraft.commands.arguments.CompoundTagArgument
import net.minecraft.core.component.DataComponents.CUSTOM_DATA
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomData

class TrapShopEntry(
    playerName: String,
    private val name: String,
    private val price: Int,
    isSnare: Boolean,
    effects: List<String>,
    triggerRange: Double,
    setupTime: Int,
    triggerVisibilityRange: Double,
    visibilityRange: Double,
    affectedRange: Double,
    triggerDuration: Int
) : ShopEntry(playerName, nameForStat = "${name}_TRAP")
{
    private val itemStack = ItemStack(Items.BAT_SPAWN_EGG)

    private val compoundTagArgument = CompoundTagArgument.compoundTag()

    init
    {
        val combinedNbtString = effects.joinToString(separator = ",", prefix = "{effects:[", postfix = "]}")

        val effectInstances = compoundTagArgument.parse(
            StringReader(
                combinedNbtString
            )
        )
            .read("effects", MobEffectInstance.CODEC.listOf())
            .orElse(emptyList())

        val nbt = CompoundTag()
        nbt.store("trapEffects", MobEffectInstance.CODEC.listOf(), effectInstances)
        nbt.putBoolean("isSnareTrap", isSnare)
        nbt.putDouble("trapTriggerRange", triggerRange)
        nbt.putInt("trapSetupTime", setupTime)
        nbt.putDouble("trapTriggerVisibilityRange", triggerVisibilityRange)
        nbt.putDouble("trapVisibilityRange", visibilityRange)
        nbt.putDouble("trapAffectedRange", affectedRange)
        nbt.putInt("trapTriggerDuration", triggerDuration)
        itemStack.set(CUSTOM_DATA, CustomData.of(nbt))

        itemStack.setCustomName(Component.literal(name).withStyle(Style.EMPTY.withItalic(false)))
    }

    override fun getPrice(): Int = price

    override fun getDisplayItemStack(): ItemStack =
        Items.BAT_SPAWN_EGG.defaultInstance.setCustomName(
            Component.literal("${MoneyManager.getCurrencyString(price)}: $name x1").withColor(
                if (getDGMoney(playerName) < price) Util.getRGBInt(123, 0, 0)
                else Util.getRGBInt(255, 255, 255)
            )
        )

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
        ) // should be null-safe, because remove will not be called, if player is null
    }
}