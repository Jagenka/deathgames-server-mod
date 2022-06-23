package de.jagenka.team

import de.jagenka.DeathGames
import de.jagenka.config.Config
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

object TeamSelectorUI
{
    val lobbyBounds = Config.configEntry.misc.lobbyBounds

    @JvmStatic
    fun showInterfaceIfInLobby(player: ServerPlayerEntity): Boolean
    {
        if (isInLobbyBounds(player) && !DeathGames.currentlyStarting)
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
                val inventory = TeamSelectorInventory(serverPlayerEntity)
                val screenHandler =
                    object : GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X2, syncId, serverPlayerEntity.inventory, inventory, 2)
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

            override fun getDisplayName(): Text = Text.of("SELECT TEAM")
        }.let {
            serverPlayerEntity.openHandledScreen(it)
        }
    }

    fun isInLobbyBounds(player: ServerPlayerEntity): Boolean = lobbyBounds.contains(player.pos.toDGCoordinates())
}