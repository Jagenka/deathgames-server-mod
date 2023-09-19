package de.jagenka.shop

import de.jagenka.managers.PlayerManager
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

object ShopEntries
{
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

    /**
     * map of playerName to their individual shop
     */
    val shops = mutableMapOf<String, PersonalizedShop>()

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