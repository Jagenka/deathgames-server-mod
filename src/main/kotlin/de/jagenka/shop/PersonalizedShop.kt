package de.jagenka.shop;

import de.jagenka.config.Config
import net.minecraft.item.ItemStack
import net.minecraft.nbt.StringNbtReader
import net.minecraft.registry.Registries
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier

class PersonalizedShop(private val player: ServerPlayerEntity)
{
    val EMPTY = ItemShopEntry(player, ItemStack.EMPTY, 0, "")

    var entries: Map<Int, ShopEntry> = emptyMap()
        private set

    init
    {
        val buffer = mutableMapOf<Int, ShopEntry>()

        Config.configEntry.shop.items.forEach { (row, col, name, id, amount, nbt, price) ->
            val itemStack = ItemStack(Registries.ITEM.getOrEmpty(Identifier(id)).orElse(null), amount)
            if (nbt.isNotBlank()) itemStack.nbt = StringNbtReader.parse(nbt)
            buffer[ShopEntries.slot(row, col)] = ItemShopEntry(player, itemStack, price, name)
        }

        Config.configEntry.shop.shield?.let { (row, col, name, durability, price) ->
            buffer[ShopEntries.slot(row, col)] = ShieldShopEntry(player, name, durability, price)
        }

        Config.configEntry.shop.extraLife?.let { (row, col, name, id, amount, nbt, price) ->
            val itemStack = ItemStack(Registries.ITEM.getOrEmpty(Identifier(id)).orElse(null), amount)
            if (nbt.isNotBlank()) itemStack.nbt = StringNbtReader.parse(nbt)
            buffer[ShopEntries.slot(row, col)] = ExtraLifeShopEntry(player, itemStack, price, name)
        }

        Config.configEntry.shop.leaveShop?.let { (row, col) -> buffer[ShopEntries.slot(row, col)] = LeaveShopEntry(player) }

        Config.configEntry.shop.upgrades.forEach { (row, col, name, id, levels) ->
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

            buffer[ShopEntries.slot(row, col)] = UpgradeableShopEntry(player, id, itemStackLists, prices, name)
        }

        Config.configEntry.shop.refunds.forEach { (row, col, targetRow, targetCol) ->
            buffer[ShopEntries.slot(row, col)] = RefundShopEntry(player, targetRow, targetCol)
        }

        Config.configEntry.shop.traps.forEach { (row, col, name, price, snare, effectNBTs, triggerRange, setupTime, triggerVisibilityRange, visibilityRange, affectedRange, triggerDuration) ->
            buffer[ShopEntries.slot(row, col)] = TrapShopEntry(
                player,
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

        Config.configEntry.shop.refundRecent?.let { (row, col, name) ->
            buffer[ShopEntries.slot(row, col)] = RefundRecentShopEntry(player, name)
        }

        Config.configEntry.shop.hook?.let { (row, col, name, price, maxDistance, cooldown) ->
            buffer[ShopEntries.slot(row, col)] = HookerShopEntry(player, name, price, maxDistance, cooldown)
        }

        entries = buffer.toMap()
    }
}
