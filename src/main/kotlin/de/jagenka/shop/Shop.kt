package de.jagenka.shop

import de.jagenka.DeathGames
import de.jagenka.config.Config
import de.jagenka.managers.MoneyManager
import de.jagenka.util.I18n
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack

object Shop
{
    /**
     * key is a pair of playerName and upgrade type
     * value is current level 0-indexed, -1 is no upgrade bought
     */
    private val currentUpgradableLevels = mutableMapOf<Pair<String, String>, Int>().withDefault { -1 }

    const val SLOT_AMOUNT = 9 * 6

    @JvmStatic
    fun showInterfaceIfInShop(player: ServerPlayer): Boolean
    {
        if (DeathGames.running && isInShopBounds(player))
        {
            showInterface(player)
            return true
        }
        return false
    }

    fun showInterface(serverPlayer: ServerPlayer)
    {
        object : MenuProvider
        {
            override fun createMenu(syncId: Int, inv: Inventory, player: Player): AbstractContainerMenu
            {
                val shopInventory = ShopInventory(serverPlayer.name.string)
                val screenHandler =
                    object : ChestMenu(MenuType.GENERIC_9x6, syncId, inv, shopInventory, 6)
                    {
                        override fun quickMoveStack(player: Player, slot: Int): ItemStack = ItemStack.EMPTY

                        override fun clicked(slotIndex: Int, button: Int, containerInput: ContainerInput, player: Player)
                        {
                            if (containerInput == ContainerInput.PICKUP) shopInventory.onClick(slotIndex)
                            player.inventoryMenu.sendAllDataToRemote()
                            serverPlayer.inventoryMenu.sendAllDataToRemote()
                        }
                    }
                return screenHandler
            }

            override fun getDisplayName(): Component = Component.literal(I18n.get("shopWindowTitle"))

        }.let {
            serverPlayer.openMenu(it)
        }
    }

    fun getLevelForUpgradeType(playerName: String, upgradeType: String): Int
    {
        return currentUpgradableLevels.getValue(playerName to upgradeType)
    }

    fun setLevelForUpgradeType(playerName: String, upgradeType: String, level: Int)
    {
        currentUpgradableLevels[playerName to upgradeType] = level
    }

    fun reset()
    {
        this.currentUpgradableLevels.clear()
    }

    fun isInShopBounds(player: Player?): Boolean
    {
        if (player == null) return false

        return Config.shopSettings.shopBounds.any { it.contains(player.position()) }

    }

    fun getNotEnoughMoneyString(price: Int) = I18n.get("notEnoughMoney", mapOf("amount" to MoneyManager.getCurrencyString(price)))

    // region refund recent

    private val recentlyClickedAmount = mutableMapOf<String, MutableMap<ShopEntry, Int>>().withDefault { mutableMapOf<ShopEntry, Int>().withDefault { 0 } }

    fun registerRecentlyBought(playerName: String, shopEntry: ShopEntry)
    {
        val count = recentlyClickedAmount[playerName]?.get(shopEntry) ?: 0
        recentlyClickedAmount.getOrPut(playerName) { mutableMapOf() }[shopEntry] = count + 1
    }

    /**
     * this should be called, whenever a shop entry no longer needs to be refunded with "refund recent"
     * e.g. when a shop entry is successfully refunded with RefundShopEntry.
     */
    fun unregisterRecentlyBought(playerName: String, shopEntry: ShopEntry)
    {
        val count = recentlyClickedAmount[playerName]?.get(shopEntry) ?: 0
        recentlyClickedAmount.getOrPut(playerName) { mutableMapOf() }[shopEntry] = count - 1
    }

    fun clearRecentlyBought(playerName: String)
    {
        recentlyClickedAmount[playerName] = mutableMapOf()
    }

    fun clearRecentlyBought(playerName: String, shopEntry: ShopEntry)
    {
        recentlyClickedAmount.getOrPut(playerName) { mutableMapOf() }[shopEntry] = 0
    }

    fun getRecentlyClickedAmounts(playerName: String): Map<ShopEntry, Int>
    {
        return recentlyClickedAmount.getValue(playerName)
    }

    // endregion
}