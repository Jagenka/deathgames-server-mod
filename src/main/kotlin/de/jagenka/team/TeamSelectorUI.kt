package de.jagenka.team

import de.jagenka.DeathGames
import de.jagenka.config.Config
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

object TeamSelectorUI
{
    val lobbyBounds = Config.configEntry.misc.lobbyBounds

    var notReadySpamProtection = false

    @JvmStatic
    fun showInterfaceIfInLobby(player: ServerPlayerEntity): Boolean
    {
        if (!DeathGames.running &&!DeathGames.currentlyStarting && isInLobbyBounds(player))
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
                        override fun quickMove(player: PlayerEntity?, slot: Int): ItemStack = ItemStack.EMPTY

                        override fun onSlotClick(slotIndex: Int, button: Int, actionType: SlotActionType?, player: PlayerEntity?)
                        {
                            if (actionType == SlotActionType.PICKUP) inventory.onClick(slotIndex)
                            serverPlayerEntity.playerScreenHandler.updateToClient()
                        }
                    }
                return screenHandler
            }

            override fun getDisplayName(): Text = Text.of(I18n.get("teamSelectWindowTitle"))
        }.let {
            serverPlayerEntity.openHandledScreen(it)
        }
    }

    fun isInLobbyBounds(player: ServerPlayerEntity): Boolean = lobbyBounds.contains(player.pos)
}