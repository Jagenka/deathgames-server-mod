package de.jagenka.shop

import de.jagenka.config.Config.configEntry
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.StringNbtReader
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Identifier

object ShopEntries
{
    val EMPTY = ItemShopEntry(ItemStack.EMPTY, 0, "")

    var shopEntries: Map<Int, ShopEntry> = emptyMap()
        private set

    fun loadShop()
    {
        val buffer = mutableMapOf<Int, ShopEntry>()

        configEntry.shop.items.forEach { (row, col, name, id, amount, nbt, price) ->
            val itemStack = ItemStack(Registries.ITEM.getOrEmpty(Identifier(id)).orElse(null), amount)
            if (nbt.isNotBlank()) itemStack.nbt = StringNbtReader.parse(nbt)
            buffer[slot(row, col)] = ItemShopEntry(itemStack, price, name)
        }

        configEntry.shop.shield?.let { (row, col, name, durability, price) ->
            buffer[slot(row, col)] = ShieldShopEntry(name, durability, price)
        }

        configEntry.shop.extraLife?.let { (row, col, name, id, amount, nbt, price) ->
            val itemStack = ItemStack(Registries.ITEM.getOrEmpty(Identifier(id)).orElse(null), amount)
            if (nbt.isNotBlank()) itemStack.nbt = StringNbtReader.parse(nbt)
            buffer[slot(row, col)] = ExtraLifeShopEntry(itemStack, price, name)
        }

        configEntry.shop.leaveShop?.let { (row, col) -> buffer[slot(row, col)] = LeaveShopEntry() }

        configEntry.shop.upgrades.forEach { (row, col, name, id, levels) ->
            val itemStackLists = mutableListOf<MutableList<ItemStack>>()
            val prices = mutableListOf<Int>()

            levels.forEach { (items, price) ->
                itemStackLists.add(items.map { (upgradeId, upgradeAmount, upgradeNbt) ->
                    val itemStack = ItemStack(Registries.ITEM.getOrEmpty(Identifier(upgradeId)).orElse(null), upgradeAmount)
                    if (upgradeNbt.isNotBlank()) itemStack.nbt = StringNbtReader.parse(upgradeNbt)
                    return@map itemStack
                }.toMutableList())
                prices.add(price)
            }

            buffer[slot(row, col)] = UpgradeableShopEntry(id, itemStackLists, prices, name)
        }

        configEntry.shop.refunds.forEach { (row, col, targetRow, targetCol) ->
            buffer[slot(row, col)] = RefundShopEntry(targetRow, targetCol)
        }

        configEntry.shop.traps.forEach { (row, col, name, price, snare, effectNBTs, triggerRange, setupTime, triggerVisibilityRange, visibilityRange, affectedRange, triggerDuration) ->
            buffer[slot(row, col)] = TrapShopEntry(
                name,
                price,
                snare,
                effectNBTs.map { StringNbtReader.parse(it) },
                triggerRange,
                setupTime,
                triggerVisibilityRange,
                visibilityRange,
                affectedRange,
                triggerDuration
            )
        }

        configEntry.shop.refundRecent?.let { (row, col, name) ->
            buffer[slot(row, col)] = RefundRecentShopEntry(name)
        }

        configEntry.shop.hook?.let { (row, col, name, price, maxDistance, cooldown) ->
            buffer[slot(row, col)] = HookerShopEntry(name, price, maxDistance, cooldown)
        }

        shopEntries = buffer.toMap()
    }

    fun Item.unbreakable(): ItemStack = ItemStack(this).makeUnbreakable()

    fun ItemStack.makeUnbreakable(): ItemStack
    {
        this.orCreateNbt.putInt("Unbreakable", 1)
        return this
    }

    fun ItemStack.withEnchantment(enchantment: Enchantment, level: Int): ItemStack
    {
        this.addEnchantment(enchantment, level)
        return this
    }

    fun ItemStack.withName(name: String): ItemStack
    {
        this.setCustomName(Text.of(name))
        return this
    }

    fun ItemStack.withDamage(damage: Int): ItemStack
    {
        this.damage = damage
        return this
    }

    fun slot(row: Int, column: Int): Int
    {
        return (row * 9 + column).coerceAtLeast(0).coerceAtMost(Shop.SLOT_AMOUNT)
    }
}