package de.jagenka.team

import de.jagenka.config.Config
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

class TeamSelectorInventory(val player: ServerPlayer) : Container
{
    private val slots: Array<UIEntry> = Array(containerSize) { EmptyUIEntry() }

    init
    {
        val enabledTeams = Config.general.enabledTeams.toList()

        val (firstLine, secondLine) =
            if (enabledTeams.size <= 7)
            {
                enabledTeams.toList() to emptyList()
            } else
            {
                val splitIndex = (enabledTeams.size + 1) / 2
                enabledTeams.subList(0, splitIndex).toList() to enabledTeams.subList(splitIndex, enabledTeams.size).toList()
            }

        val firstLineLeftIndex = 1 + (7 - firstLine.size) / 2
        val splitFirstLine = firstLine.size % 2 == 0
        firstLine.forEachIndexed { index, team ->
            if (splitFirstLine && index >= firstLine.size / 2)
            {
                slots[firstLineLeftIndex + index + 1] = TeamUIEntry(team)
            } else
            {
                slots[firstLineLeftIndex + index] = TeamUIEntry(team)
            }
        }

        val secondLineLeftIndex = 10 + (7 - secondLine.size) / 2
        val splitSecondLine = secondLine.size % 2 == 0
        secondLine.forEachIndexed { index, team ->
            if (splitSecondLine && index >= secondLine.size / 2)
            {
                slots[secondLineLeftIndex + index + 1] = TeamUIEntry(team)
            } else
            {
                slots[secondLineLeftIndex + index] = TeamUIEntry(team)
            }
        }

        slots[0] = SpectatorUIEntry()
        slots[9] = SpectatorUIEntry()

        slots[8] = ReadyUIEntry(player)
        slots[17] = StartGameUIEntry()
    }

    override fun getItem(slotIndex: Int): ItemStack
    {
        if (slotIndex !in slots.indices) return ItemStack.EMPTY
        return slots[slotIndex].displayItemStack
    }

    fun onClick(slotIndex: Int)
    {
        if (slotIndex !in slots.indices) return
        slots[slotIndex].onClick(player)
    }

    override fun clearContent() = Unit
    override fun getContainerSize(): Int = 18 // 2 * 9
    override fun isEmpty(): Boolean = false
    override fun removeItem(slot: Int, amount: Int): ItemStack = ItemStack.EMPTY
    override fun removeItemNoUpdate(slot: Int): ItemStack = ItemStack.EMPTY
    override fun setItem(slot: Int, stack: ItemStack) = Unit
    override fun setChanged() = Unit
    override fun stillValid(player: Player): Boolean = true
}