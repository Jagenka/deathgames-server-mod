package de.jagenka.team

import de.jagenka.DeathGames
import de.jagenka.config.Config
import de.jagenka.util.I18n
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack

object TeamSelectorUI
{
    val lobbyBounds = Config.general.lobbyBounds

    var notReadySpamProtection = false

    @JvmStatic
    fun showInterfaceIfInLobby(player: ServerPlayer): Boolean
    {
        if (!DeathGames.running &&!DeathGames.currentlyStarting && isInLobbyBounds(player))
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
                val teamSelectorInv = TeamSelectorInventory(serverPlayer)
                val screenHandler =
                    object : ChestMenu(MenuType.GENERIC_9x2, syncId, serverPlayer.inventory, teamSelectorInv, 2)
                    {
                        override fun quickMoveStack(player: Player, slot: Int): ItemStack = ItemStack.EMPTY

                        override fun clicked(slotIndex: Int, button: Int, actionType: ClickType, player: Player)
                        {
                            if (actionType == ClickType.PICKUP) teamSelectorInv.onClick(slotIndex)
                            serverPlayer.inventoryMenu.sendAllDataToRemote()
                        }
                    }
                return screenHandler
            }

            override fun getDisplayName(): Component = Component.literal(I18n.get("teamSelectWindowTitle"))
        }.let {
            serverPlayer.openMenu(it)
        }
    }

    fun isInLobbyBounds(player: ServerPlayer): Boolean = lobbyBounds.contains(player.position())
}