package de.jagenka.shop

import de.jagenka.DeathGames
import de.jagenka.config.Config
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
    private val upgrades = mutableMapOf<String, MutableMap<UpgradeType, Int>>().withDefault { mutableMapOf<UpgradeType, Int>().withDefault { 0 } }

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
                        override fun transferSlot(player: PlayerEntity?, index: Int): ItemStack = ItemStack.EMPTY

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

    fun getUpgradeLevel(playerName: String, upgradeType: UpgradeType) = upgrades.getValue(playerName).getValue(upgradeType)
    fun setUpgradeLevel(playerName: String, upgradeType: UpgradeType, level: Int)
    {
        val upgradeTypeIntMutableMap = upgrades.getValue(playerName)
        upgradeTypeIntMutableMap[upgradeType] = level
        upgrades[playerName] = upgradeTypeIntMutableMap
    }

    fun increaseUpgradeLevel(playerName: String, upgradeType: UpgradeType)
    {
        setUpgradeLevel(playerName, upgradeType, getUpgradeLevel(playerName, upgradeType) + 1)
    }

    fun reset()
    {
        this.upgrades.clear()
    }

    fun isInShopBounds(player: ServerPlayerEntity): Boolean = Config.shopBounds.any { it.contains(player.pos) }

    fun getNotEnoughMoneyString(price: Int) = I18n.get("notEnoughMoney", mapOf("amount" to MoneyManager.getCurrencyString(price)))
}

enum class UpgradeType
{
    SWORD, AXE, BOW, CROSSBOW, ARMOR
}