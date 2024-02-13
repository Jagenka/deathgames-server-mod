package de.jagenka.team

import de.jagenka.DeathGames
import de.jagenka.managers.DisplayManager
import de.jagenka.managers.DisplayManager.sendPrivateMessage
import de.jagenka.managers.PlayerManager
import de.jagenka.managers.PlayerManager.addToDGTeam
import de.jagenka.managers.PlayerManager.kickFromDGTeam
import de.jagenka.team.TeamSelectorUI.notReadySpamProtection
import de.jagenka.timer.Timer
import de.jagenka.timer.seconds
import de.jagenka.util.I18n
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
            val baseString = I18n.get("teamSelectUIHover", mapOf("teamSize" to teamSize, "teamName" to "%teamName")) //TODO: geht das besser?
            return team.getColorBlock().asItem().defaultStack.setCustomName(DisplayManager.getTextWithPlayersAndTeamsColored(baseString, idToTeam = mapOf("%teamName" to team)))
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
        get() = ItemStack(Items.ENDER_EYE).setCustomName(Text.of(I18n.get("spectator")))

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
                ItemStack(Items.LIME_DYE).setCustomName(literal(I18n.get("ready")))
            } else
            {
                ItemStack(Items.RED_DYE).setCustomName(literal(I18n.get("notReady")))
            }
        }

    override fun onClick(player: ServerPlayerEntity)
    {
        val playerName = player.name.string
        if (ReadyCheck.isReady(playerName))
        {
            ReadyCheck.makeUnready(playerName)
            player.sendPrivateMessage(I18n.get("noLongerReady"))
        } else
        {
            ReadyCheck.makeReady(playerName)
            player.sendPrivateMessage(I18n.get("nowReady"))
        }
    }
}

class StartGameUIEntry : UIEntry
{
    override val displayItemStack: ItemStack
        get() = ItemStack(Items.AXOLOTL_BUCKET).setCustomName(literal(I18n.get("startGame")))

    override fun onClick(player: ServerPlayerEntity)
    {
        val whoIsNotReady = ReadyCheck.whoIsNotReady()
        if (whoIsNotReady.isEmpty())
        {
            DeathGames.startGameWithCountdown()
            ReadyCheck.clear()
        } else
        {
            if (notReadySpamProtection) return
            notReadySpamProtection = true
            Timer.schedule(1.seconds()) { notReadySpamProtection = false }

            var whoIsNotReadyString = ""
            repeat(whoIsNotReady.size) { index ->
                if (index != 0)
                {
                    whoIsNotReadyString +=
                        if (index == whoIsNotReady.lastIndex)
                        {
                            " ${I18n.get("and")} "
                        } else
                        {
                            ", "
                        }
                }
                whoIsNotReadyString += "%playerName$index"
            }

            val notReadyString = I18n.get("cantStartGame", mapOf("players" to whoIsNotReadyString)) //TODO: besser?
            val idToPlayer = whoIsNotReady.mapIndexed { index, playerName -> index to playerName }.associate { (index, playerName) -> "%playerName$index" to playerName }
            //println(idToPlayer)
            DisplayManager.sendChatMessage(DisplayManager.getTextWithPlayersAndTeamsColored(notReadyString, idToPlayer = idToPlayer))
        }
    }
}

class EmptyUIEntry : UIEntry
{
    override val displayItemStack: ItemStack
        get() = ItemStack.EMPTY

    override fun onClick(player: ServerPlayerEntity) = Unit
}