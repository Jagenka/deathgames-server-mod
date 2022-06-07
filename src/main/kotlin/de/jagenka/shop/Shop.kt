package de.jagenka.shop

import de.jagenka.BlockCuboid
import de.jagenka.Coordinates
import de.jagenka.config.Config
import de.jagenka.managers.getDGMoney
import de.jagenka.toDGCoordinates
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
    const val SHOP_UNIT = "$"

    private val upgrades = mutableMapOf<ServerPlayerEntity, MutableMap<UpgradeType, Int>>().withDefault { mutableMapOf<UpgradeType, Int>().withDefault { 0 } }

    const val slotAmount = 9 * 6

    @JvmStatic
    fun showInterfaceIfInShop(player: ServerPlayerEntity): Boolean
    {
        if (isInShopBounds(player))
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
                        override fun transferSlot(player: PlayerEntity?, index: Int): ItemStack
                        {
                            return ItemStack.EMPTY
                        }

                        override fun onSlotClick(slotIndex: Int, button: Int, actionType: SlotActionType?, player: PlayerEntity?)
                        {
                            inventory.onClick(slotIndex)
                        }
                    }
                return screenHandler
            }

            override fun getDisplayName(): Text = Text.of("SHOP")
        }
            .let {
                serverPlayerEntity.openHandledScreen(it)
            }
    }

    fun getUpgradeLevel(player: ServerPlayerEntity, upgradeType: UpgradeType) = upgrades.getValue(player).getValue(upgradeType)
    fun setUpgradeLevel(player: ServerPlayerEntity, upgradeType: UpgradeType, level: Int)
    {
        val upgradeTypeIntMutableMap = upgrades.getValue(player)
        upgradeTypeIntMutableMap[upgradeType] = level
        upgrades[player] = upgradeTypeIntMutableMap
    }

    fun increaseUpgradeLevel(player: ServerPlayerEntity, upgradeType: UpgradeType)
    {
        setUpgradeLevel(player, upgradeType, getUpgradeLevel(player, upgradeType) + 1)
    }

    fun getBalanceString(player: ServerPlayerEntity) = "You have $SHOP_UNIT${player.getDGMoney()} to spend."

    fun reset()
    {
        this.upgrades.clear()
    }

    fun isInShopBounds(player: ServerPlayerEntity): Boolean = Config.shopBounds.contains(player.pos.toDGCoordinates())
}

enum class UpgradeType
{
    SWORD, AXE, BOW, CROSSBOW, ARMOR
}