package de.jagenka.shop

import de.jagenka.DeathGames
import de.jagenka.config.Config
import de.jagenka.floor
import de.jagenka.managers.MoneyManager
import de.jagenka.util.I18n
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

object Shop
{
    private val upgrades = mutableMapOf<String, MutableMap<String, Int>>().withDefault { mutableMapOf<String, Int>().withDefault { 0 } }

    const val slotAmount = 9 * 6

    @JvmStatic
    fun showInterfaceIfInShop(player: ServerPlayerEntity): Boolean
    {
        if (DeathGames.running && isInShopBounds(player))
        {
            showInterface(player)
            return true
        }
        return false
    }

    fun showInterface(serverPlayerEntity: ServerPlayerEntity)
    {
        object : NamedScreenHandlerFactory
        {
            override fun createMenu(syncId: Int, inv: PlayerInventory?, player: PlayerEntity?): ScreenHandler
            {
                val inventory = ShopInventory(serverPlayerEntity)
                val screenHandler =
                    object : GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X6, syncId, serverPlayerEntity.inventory, inventory, 6)
                    {
                        override fun quickMove(player: PlayerEntity?, slot: Int): ItemStack = ItemStack.EMPTY

                        override fun onSlotClick(slotIndex: Int, button: Int, actionType: SlotActionType?, player: PlayerEntity?)
                        {
                            if (actionType == SlotActionType.PICKUP) inventory.onClick(slotIndex)
                            serverPlayerEntity.playerScreenHandler.updateToClient()
                        }
                    }
                return screenHandler
            }

            override fun getDisplayName(): Text = Text.of(I18n.get("shopWindowTitle"))
        }.let {
            serverPlayerEntity.openHandledScreen(it)
        }
    }

    fun getUpgradeLevel(playerName: String, upgradeType: String) = upgrades.getValue(playerName).getValue(upgradeType)
    fun setUpgradeLevel(playerName: String, upgradeType: String, level: Int)
    {
        val upgradeTypeIntMutableMap = upgrades.getValue(playerName)
        upgradeTypeIntMutableMap[upgradeType] = level
        upgrades[playerName] = upgradeTypeIntMutableMap
    }

    fun increaseUpgradeLevel(playerName: String, upgradeType: String)
    {
        setUpgradeLevel(playerName, upgradeType, getUpgradeLevel(playerName, upgradeType) + 1)
    }

    fun reset()
    {
        this.upgrades.clear()
    }

    fun isInShopBounds(player: PlayerEntity): Boolean = Config.shopBounds.any { it.contains(player.pos) }

    fun getNotEnoughMoneyString(price: Int) = I18n.get("notEnoughMoney", mapOf("amount" to MoneyManager.getCurrencyString(price)))

    private val recentlyBought = mutableMapOf<String, MutableList<ShopEntry>>().withDefault { mutableListOf() }

    fun registerRecentlyBought(playerName: String, shopEntry: ShopEntry)
    {
        val list = recentlyBought.getValue(playerName)
        list.add(shopEntry)
        recentlyBought[playerName] = list
    }

    fun clearRecentlyBought(playerName: String) = recentlyBought.getValue(playerName).clear()

    fun getRecentlyBought(playerName: String) = recentlyBought.getValue(playerName).toList()

    fun getRefundAmount(player: ServerPlayerEntity, shopEntryToRefund: ShopEntry) =
        (shopEntryToRefund.getTotalSpentMoney(player) * (Config.refundPercent.toDouble() / 100.0)).floor()
}