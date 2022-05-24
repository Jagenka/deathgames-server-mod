package de.jagenka.shop

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
    fun showInterface(serverPlayerEntity: ServerPlayerEntity)
    {
        object : NamedScreenHandlerFactory
        {
            override fun createMenu(syncId: Int, inv: PlayerInventory?, player: PlayerEntity?): ScreenHandler
            {
                val inventory = ShopInventory(serverPlayerEntity)
                val screenHandler = object : GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, 69, serverPlayerEntity.inventory, inventory, 3)
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

            override fun getDisplayName(): Text = Text.of("GIN-O")
        }.let { serverPlayerEntity.openHandledScreen(it) }
    }
}