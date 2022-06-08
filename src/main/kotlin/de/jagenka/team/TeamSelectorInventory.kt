package de.jagenka.team

import de.jagenka.managers.DisplayManager
import de.jagenka.managers.PlayerManager.addToDGTeam
import de.jagenka.managers.PlayerManager.kickFromDGTeam
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemStack.EMPTY
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class TeamSelectorInventory(val player: ServerPlayerEntity) : Inventory
{
    private val slots: Array<DGTeam?> = Array(size()) { null }

    init
    {
        val numberOfTeamsHalf = DGTeam.values().size / 2
        DGTeam.values().forEachIndexed { index, team ->
            if (index < numberOfTeamsHalf)
            {
                slots[index + (9 - numberOfTeamsHalf) / 2] = team
            } else
            {
//                slots[9 + index - numberOfTeamsHalf + (9 - numberOfTeamsHalf) / 2] = team
                slots[index + (9 - numberOfTeamsHalf) * 3 / 2] = team
            }
        }
    }

    override fun getStack(slotIndex: Int): ItemStack
    {
        return slots[slotIndex]?.let {
            it.getColorBlock().asItem().defaultStack.setCustomName(Text.of(it.name).getWithStyle(Style.EMPTY.withColor(Formatting.byName(it.name.lowercase())))[0])
        } ?: Items.ENDER_EYE.defaultStack.setCustomName(Text.of("Spectator"))
    }

    fun onClick(slotIndex: Int)
    {
        slots[slotIndex]?.let {
            player.addToDGTeam(it)

            val base = Text.literal("")
            base.append(Text.of("${player.name.string} joined Team "))
            base.append(Text.of(it.name).getWithStyle(Style.EMPTY.withColor(Formatting.byName(it.name.lowercase())))[0])
            DisplayManager.sendChatMessage(base)
        } ?: player.kickFromDGTeam().also {
            DisplayManager.sendChatMessage(Text.of("${player.name.string} wants to spectate."))
        }
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