package de.jagenka.shop;

import com.mojang.brigadier.StringReader
import de.jagenka.DeathGames
import de.jagenka.config.Config
import net.minecraft.command.argument.ItemStringReader
import net.minecraft.item.ItemStack
import net.minecraft.nbt.StringNbtReader

class PersonalizedShop(private val playerName: String)
{
    val EMPTY = ItemShopEntry(playerName, ItemStack.EMPTY, 0, "")

    var entries: Map<Int, ShopEntry> = emptyMap()
        private set

    private val itemStringReader = ItemStringReader(DeathGames.commandRegistryAccess)

    init
    {
        val buffer = mutableMapOf<Int, ShopEntry>()

        Config.shop.items.forEach { (row, col, name, id, amount, nbt, price) ->
            buffer[slot(row, col)] = ItemShopEntry(playerName, itemStack(id, nbt, amount), price, name)
        }

        Config.shop.shield?.let { (row, col, name, durability, price) ->
            buffer[slot(row, col)] = ShieldShopEntry(playerName, name, durability, price)
        }

        Config.shop.extraLife?.let { (row, col, name, id, amount, nbt, price) ->
            buffer[slot(row, col)] = ExtraLifeShopEntry(playerName, itemStack(id, nbt, amount), price, name)
        }

        Config.shop.leaveShop?.let { (row, col) -> buffer[slot(row, col)] = LeaveShopEntry(playerName) }

        Config.shop.upgrades.forEach { (row, col, name, id, levels) ->
            val itemStackLists = mutableListOf<MutableList<ItemStack>>()
            val prices = mutableListOf<Int>()

            levels.forEach { (items, price) ->
                itemStackLists.add(items.map { (upgradeId, upgradeAmount, upgradeNbt) ->
                    itemStack(upgradeId, upgradeNbt, upgradeAmount)
                }.toMutableList())
                prices.add(price)
            }

            buffer[slot(row, col)] = UpgradeableShopEntry(playerName, id, itemStackLists, prices, name)
        }

        Config.shop.traps.forEach { (row, col, name, price, snare, effectNBTs, triggerRange, setupTime, triggerVisibilityRange, visibilityRange, affectedRange, triggerDuration) ->
            buffer[slot(row, col)] = TrapShopEntry(
                playerName,
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

        Config.shop.refundRecent?.let { (row, col, name) ->
            buffer[slot(row, col)] = RefundRecentShopEntry(playerName, name)
        }

        Config.shop.hook?.let { (row, col, name, price, maxDistance, cooldown) ->
            buffer[slot(row, col)] = HookerShopEntry(playerName, name, price, maxDistance, cooldown)
        }

        // refunds need to be last
        Config.shop.refunds.forEach { (row, col, targetRow, targetCol) ->
            buffer[slot(row, col)] = RefundShopEntry(playerName, buffer[slot(targetRow, targetCol)] ?: return@forEach)
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
