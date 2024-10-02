package de.jagenka.shop;

import com.mojang.brigadier.StringReader
import de.jagenka.config.Config
import net.minecraft.command.argument.ItemStringReader
import net.minecraft.item.ItemStack
import net.minecraft.nbt.StringNbtReader
import net.minecraft.registry.BuiltinRegistries
import net.minecraft.server.command.CommandManager
import net.minecraft.server.network.ServerPlayerEntity

class PersonalizedShop(private val player: ServerPlayerEntity)
{
    val EMPTY = ItemShopEntry(player, ItemStack.EMPTY, 0, "")

    var entries: Map<Int, ShopEntry> = emptyMap()
        private set

    private val commandRegistryAccess = CommandManager.createRegistryAccess(BuiltinRegistries.createWrapperLookup())
    private val itemStringReader = ItemStringReader(commandRegistryAccess)

    init
    {
        val buffer = mutableMapOf<Int, ShopEntry>()

        Config.configEntry.shop.items.forEach { (row, col, name, id, amount, nbt, price) ->
            buffer[slot(row, col)] = ItemShopEntry(player, itemStack(id, nbt, amount), price, name)
        }

        Config.configEntry.shop.shield?.let { (row, col, name, durability, price) ->
            buffer[slot(row, col)] = ShieldShopEntry(player, name, durability, price)
        }

        Config.configEntry.shop.extraLife?.let { (row, col, name, id, amount, nbt, price) ->
            buffer[slot(row, col)] = ExtraLifeShopEntry(player, itemStack(id, nbt, amount), price, name)
        }

        Config.configEntry.shop.leaveShop?.let { (row, col) -> buffer[slot(row, col)] = LeaveShopEntry(player) }

        Config.configEntry.shop.upgrades.forEach { (row, col, name, id, levels) ->
            val itemStackLists = mutableListOf<MutableList<ItemStack>>()
            val prices = mutableListOf<Int>()

            levels.forEach { (items, price) ->
                itemStackLists.add(items.map { (upgradeId, upgradeAmount, upgradeNbt) ->
                    itemStack(upgradeId, upgradeNbt, upgradeAmount)
                }.toMutableList())
                prices.add(price)
            }

            buffer[slot(row, col)] = UpgradeableShopEntry(player, id, itemStackLists, prices, name)
        }

        Config.configEntry.shop.traps.forEach { (row, col, name, price, snare, effectNBTs, triggerRange, setupTime, triggerVisibilityRange, visibilityRange, affectedRange, triggerDuration) ->
            buffer[slot(row, col)] = TrapShopEntry(
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
            buffer[slot(row, col)] = RefundRecentShopEntry(player, name)
        }

        Config.configEntry.shop.hook?.let { (row, col, name, price, maxDistance, cooldown) ->
            buffer[slot(row, col)] = HookerShopEntry(player, name, price, maxDistance, cooldown)
        }

        // refunds need to be last
        Config.configEntry.shop.refunds.forEach { (row, col, targetRow, targetCol) ->
            buffer[slot(row, col)] = RefundShopEntry(player, buffer[slot(targetRow, targetCol)] ?: return@forEach)
        }

        entries = buffer.toMap()
    }

    private fun itemStack(id: String, nbt: String, amount: Int): ItemStack
    {
        val itemResult = itemStringReader.consume(StringReader(id + nbt))
        val itemStack = ItemStack(itemResult.item, amount)
        itemStack.applyUnvalidatedChanges(itemResult.components)
        return itemStack
    }

    fun slot(row: Int, column: Int): Int
    {
        return (row * 9 + column).coerceAtLeast(0).coerceAtMost(Shop.SLOT_AMOUNT)
    }
}
