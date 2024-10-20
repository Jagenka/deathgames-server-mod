package de.jagenka.shop

import de.jagenka.managers.PlayerManager

object ShopEntries
{
    /**
     * map of playerName to their individual shop
     */
    private val shops = mutableMapOf<String, PersonalizedShop>()

    fun getShopFor(playerName: String): PersonalizedShop
    {
        return shops.getOrPut(playerName) { PersonalizedShop(playerName) }
    }

    fun loadShop()
    {
        PlayerManager.getOnlinePlayers().forEach {
            shops.putIfAbsent(it.name.string, PersonalizedShop(it.name.string))
        }
    }

    fun reloadShop()
    {
        shops.clear()
        loadShop()
    }
}