package de.jagenka.shop

import net.minecraft.item.ItemStack
import kotlin.math.max
import kotlin.math.min

open class IndexOffsetList<T>(protected val list: List<T>)
{
    open operator fun get(level: Int): T?
    {
        if (level !in this) return null
        return list[level - 1]
    }

    operator fun contains(level: Int) = (level - 1) in list.indices

    fun canUpgrade(currentLevel: Int) = currentLevel + 1 in this
    fun forEach(action: (T) -> Unit)
    {
        list.forEach(action)
    }

    fun joinToString(
        separator: CharSequence = ", ",
        prefix: CharSequence = "",
        postfix: CharSequence = "",
        limit: Int = -1,
        truncated: CharSequence = "...",
        transform: ((T) -> CharSequence)? = null
    ): String
    {
        return list.joinToString(separator, prefix, postfix, limit, truncated, transform)
    }
}

class ItemsList(items: List<List<ItemStack>>) : IndexOffsetList<List<ItemStack>>(items)
{
    override fun get(level: Int): List<ItemStack>
    {
        return super.get(level) ?: emptyList()
    }
}

class PriceList(priceList: List<Int>) : IndexOffsetList<Int>(priceList)
{
    override fun get(level: Int): Int
    {
        return super.get(level) ?: 0
    }

    fun sumBetween(currentLevel: Int, targetLevel: Int): Int
    {
        return (min(currentLevel, targetLevel) + 1).rangeUntil(max(currentLevel, targetLevel) + 1) // range from lower to higher value, else range is empty
            .toList().sumOf { list[it] } // sum up level's costs
    }
}