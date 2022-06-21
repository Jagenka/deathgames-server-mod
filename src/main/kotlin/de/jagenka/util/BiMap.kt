package de.jagenka.util

class BiMap<K, V>
{
    private var map = mutableMapOf<K, V>()
    private var inv = mutableMapOf<V, K>()

    fun clear()
    {
        map.clear()
        inv.clear()
    }

    fun put(key: K, value: V)
    {
        inv.remove(map[key])
        map.remove(inv[value])
        map[key] = value
        inv[value] = key
    }

    fun containsKey(key: K): Boolean
    {
        return map.containsKey(key)
    }

    fun containsValue(value: V): Boolean
    {
        return inv.containsKey(value)
    }

    fun getValueForKey(key: K): V?
    {
        return map[key]
    }

    fun getKeyForValue(value: V): K?
    {
        return inv[value]
    }

    fun keys(): Set<K>
    {
        return map.keys
    }

    fun values(): Set<V>
    {
        return inv.keys
    }

    fun getAsSet(): Set<Pair<K, V>>
    {
        val set = HashSet<Pair<K, V>>()
        map.forEach { (key, value) -> set.add(Pair(key, value)) }
        return set
    }

    fun removeForKey(key: K)
    {
        inv.remove(map.remove(key))
    }

    fun removeForValue(value: V)
    {
        map.remove(inv.remove(value))
    }

    operator fun get(key: K) = getValueForKey(key)
    operator fun set(key: K, value: V) = put(key, value)

}