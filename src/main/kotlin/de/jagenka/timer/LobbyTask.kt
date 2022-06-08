package de.jagenka.timer

import de.jagenka.managers.DisplayManager
import de.jagenka.managers.PlayerManager
import de.jagenka.team.TeamSelectorUI
import net.minecraft.text.Text

object LobbyTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = false
    override val runEvery: Int
        get() = 1.ticks()

    override fun run()
    {
        PlayerManager.getOnlinePlayers().forEach { player ->
            if(TeamSelectorUI.isInLobbyBounds(player))
            {
                DisplayManager.sendMessageToHotbar(Text.of("Press F to choose your team."))
            }
        }
    }

    override fun reset()
    {

    }
}