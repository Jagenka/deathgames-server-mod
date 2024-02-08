package de.jagenka.team

import de.jagenka.config.Config
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
        val enabledTeams = Config.configEntry.general.enabledTeams.toList()

        val (firstLine, secondLine) =
            if (enabledTeams.size <= 7)
            {
                enabledTeams.toList() to emptyList()
            } else
            {
                enabledTeams.subList(0, (enabledTeams.size + 1) / 2).toList() to enabledTeams.subList((enabledTeams.size + 1) / 2, enabledTeams.size).toList()
            }

        val firstLineLeftIndex = 1 + (7 - firstLine.size) / 2
        firstLine.forEachIndexed { index, team ->
            slots[firstLineLeftIndex + index] = TeamUIEntry(team)
        }

        val secondLineLeftIndex = 10 + (7 - secondLine.size) / 2
        secondLine.forEachIndexed { index, team ->
            slots[secondLineLeftIndex + index] = TeamUIEntry(team)
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