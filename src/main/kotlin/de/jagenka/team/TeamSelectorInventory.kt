package de.jagenka.team

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemStack.EMPTY
import net.minecraft.server.network.ServerPlayerEntity

class TeamSelectorInventory(val player: ServerPlayerEntity) : Inventory
{
    private val slots: Array<UIEntry> = Array(size()) { EmptyUIEntry() }

    init
    {
        val numberOfTeamsHalf = DGTeam.entries.size / 2

        DGTeam.entries.forEachIndexed { index, team ->
            if (index < numberOfTeamsHalf)
            {
                slots[index + 1] = TeamUIEntry(team)
            } else
            {
                slots[index - numberOfTeamsHalf + 10] = TeamUIEntry(team)
            }
        }

        slots[0] = SpectatorUIEntry()
        slots[9] = SpectatorUIEntry()

        slots[8] = ReadyUIEntry(player)
        slots[17] = StartGameUIEntry()
    }

    override fun getStack(slotIndex: Int): ItemStack
    {
        return slots[slotIndex].displayItemStack
    }

    fun onClick(slotIndex: Int)
    {
        slots[slotIndex].onClick(player)
    }

    override fun clear() = Unit
    override fun size(): Int = 18 // 2 * 9
    override fun isEmpty(): Boolean = false
    override fun removeStack(slot: Int, amount: Int): ItemStack = EMPTY
    override fun removeStack(slot: Int): ItemStack = EMPTY
    override fun setStack(slot: Int, stack: ItemStack?) = Unit
    override fun markDirty() = Unit
    override fun canPlayerUse(player: PlayerEntity?): Boolean = true
    override fun onOpen(player: PlayerEntity?) = Unit
}