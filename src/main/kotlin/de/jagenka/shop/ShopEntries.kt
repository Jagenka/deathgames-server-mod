package de.jagenka.shop

import de.jagenka.managers.PlayerManager
import net.minecraft.server.network.ServerPlayerEntity

object ShopEntries
{
    /**
     * map of playerName to their individual shop
     */
    private val shops = mutableMapOf<String, PersonalizedShop>()

    fun getShopFor(player: ServerPlayerEntity): PersonalizedShop
    {
        return shops.getOrPut(player.name.string) { PersonalizedShop(player) }
    }

    fun loadShop()
    {
        PlayerManager.getOnlinePlayers().forEach {
            shops.putIfAbsent(it.name.string, PersonalizedShop(it))
        }
    }

    fun reloadShop()
    {
        shops.clear()
        loadShop()
    }
}