package de.jagenka.shop;

import de.jagenka.Util.parseItemStack
import de.jagenka.config.Config
import net.minecraft.item.ItemStack

class PersonalizedShop(private val playerName: String)
{
    val EMPTY = ItemShopEntry(playerName, ItemStack.EMPTY, 0, "")

    var entries: Map<Int, ShopEntry> = emptyMap()
        private set

    init
    {
        val buffer = mutableMapOf<Int, ShopEntry>()

        Config.shop.items.forEach { (row, col, name, id, amount, nbt, price) ->
            buffer[slot(row, col)] = ItemShopEntry(playerName, parseItemStack(id, nbt, amount), price, name)
        }

        Config.shop.shield?.let { (row, col, name, durability, price) ->
            buffer[slot(row, col)] = ShieldShopEntry(playerName, name, durability, price)
        }

        Config.shop.extraLife?.let { (row, col, name, id, amount, nbt, price) ->
            buffer[slot(row, col)] = ExtraLifeShopEntry(playerName, parseItemStack(id, nbt, amount), price, name)
        }

        Config.shop.leaveShop?.let { (row, col) -> buffer[slot(row, col)] = LeaveShopEntry(playerName) }

        Config.shop.upgrades.forEach { (row, col, name, id, levels) ->
            val itemStackLists = mutableListOf<MutableList<ItemStack>>()
            val prices = mutableListOf<Int>()

            levels.forEach { (items, price) ->
                itemStackLists.add(items.map { (upgradeId, upgradeAmount, upgradeNbt) ->
                    parseItemStack(upgradeId, upgradeNbt, upgradeAmount)
                }.toMutableList())
                prices.add(price)
            }

            buffer[slot(row, col)] = UpgradeableShopEntry(playerName, id, itemStackLists, prices, name)
        }

        Config.shop.traps.forEach { (row, col, name, price, snare, effectNBTStrings, triggerRange, setupTime, triggerVisibilityRange, visibilityRange, affectedRange, triggerDuration) ->
            buffer[slot(row, col)] = TrapShopEntry(
                playerName,
                name,
                price,
                snare,
                effectNBTStrings,
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

    fun slot(row: Int, column: Int): Int
    {
        return (row * 9 + column).coerceAtLeast(0).coerceAtMost(Shop.SLOT_AMOUNT)
    }
}
