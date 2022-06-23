package de.jagenka.team

import de.jagenka.DeathGames
import de.jagenka.managers.DisplayManager
import de.jagenka.managers.DisplayManager.sendPrivateMessage
import de.jagenka.managers.PlayerManager
import de.jagenka.managers.PlayerManager.addToDGTeam
import de.jagenka.managers.PlayerManager.kickFromDGTeam
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.text.Text.literal

object ReadyCheck
{
    private val readyPlayers = mutableSetOf<String>()

    fun makeReady(playerName: String) = readyPlayers.add(playerName)
    fun makeUnready(playerName: String) = readyPlayers.remove(playerName)
    fun toggleReady(playerName: String) = if (isReady(playerName)) makeUnready(playerName) else makeReady(playerName)

    fun isReady(playerName: String) = playerName in readyPlayers

    fun whoIsNotReady() = PlayerManager.getOnlinePlayers().map { it.name.string }.filter { it !in readyPlayers }

    fun everyoneReady() = whoIsNotReady().isEmpty()

    fun clear() = readyPlayers.clear()
}

interface UIEntry
{
    val team: DGTeam?
        get() = null

    val displayItemStack: ItemStack

    fun onClick(player: ServerPlayerEntity)
}

class TeamUIEntry(override val team: DGTeam) : UIEntry
{
    override val displayItemStack: ItemStack
        get()
        {
            val teamSize = team.getPlayers().size

            val suffix = literal(
                " (${
                    if (teamSize == 0) "Empty" else
                    {
                        "$teamSize Player${if (teamSize != 1) "s" else ""}"
                    }
                })"
            )
            return team.getColorBlock().asItem().defaultStack.setCustomName(
                literal("Join ").append(team.getFormattedText()).append(suffix)
            )
        }

    override fun onClick(player: ServerPlayerEntity)
    {
        if (player.addToDGTeam(team))
        {
            DisplayManager.displayMessageOnPlayerTeamJoin(player, team)
        }
    }
}

class SpectatorUIEntry : UIEntry
{
    override val displayItemStack: ItemStack
        get() = ItemStack(Items.ENDER_EYE).setCustomName(literal("Spectator"))

    override fun onClick(player: ServerPlayerEntity)
    {
        if (player.kickFromDGTeam())
        {
            DisplayManager.displayMessageOnPlayerTeamJoin(player, null)
        }
    }
}

class ReadyUIEntry(val player: ServerPlayerEntity) : UIEntry
{
    override val displayItemStack: ItemStack
        get()
        {
            return if (ReadyCheck.isReady(player.name.string))
            {
                ItemStack(Items.LIME_DYE).setCustomName(literal("Unready"))
            } else
            {
                ItemStack(Items.RED_DYE).setCustomName(literal("Ready"))
            }
        }

    override fun onClick(player: ServerPlayerEntity)
    {
        val playerName = player.name.string
        if (ReadyCheck.isReady(playerName))
        {
            ReadyCheck.makeUnready(playerName)
            player.sendPrivateMessage("You are no longer ready.")
        } else
        {
            ReadyCheck.makeReady(playerName)
            player.sendPrivateMessage("You are now ready.")
        }
    }
}

class StartGameUIEntry : UIEntry
{
    override val displayItemStack: ItemStack
        get() = ItemStack(Items.AXOLOTL_BUCKET).setCustomName(literal("Start Game"))

    override fun onClick(player: ServerPlayerEntity)
    {
        val whoIsNotReady = ReadyCheck.whoIsNotReady()
        if (whoIsNotReady.isEmpty())
        {
            DeathGames.startGameWithCountdown()
            ReadyCheck.clear()
        } else
        {
            DisplayManager.sendChatMessage(literal("Can't start game! Not ready: ${whoIsNotReady.toString().removeSurrounding("[", "]")}"))
        }
    }
}

class EmptyUIEntry : UIEntry
{
    override val displayItemStack: ItemStack
        get() = ItemStack.EMPTY

    override fun onClick(player: ServerPlayerEntity) = Unit
}