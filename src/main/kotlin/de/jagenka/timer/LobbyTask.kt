package de.jagenka.timer

import de.jagenka.DeathGames
import de.jagenka.managers.DisplayManager
import de.jagenka.managers.PlayerManager
import de.jagenka.team.TeamSelectorUI
import de.jagenka.util.I18n
import net.minecraft.text.Text

object LobbyTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = false
    override val runEvery: Int
        get() = 1.ticks()

    override fun run()
    {
        if (DeathGames.running) return

        DisplayManager.resetBossBars()

        PlayerManager.getOnlinePlayers().forEach { player ->
            if (!player.hasPermissionLevel(2))
            {
                player.inventory.clear()
            }
            if (TeamSelectorUI.isInLobbyBounds(player))
            {
                if (!DeathGames.currentlyStarting)
                {
                    DisplayManager.sendMessageToHotbar(Text.of(I18n.get("openTeamUI")))
                }

                player.health = 20f //set max hearts
                player.hungerManager.add(20, 1f) //set max food and saturation
            }
        }
    }

    override fun reset()
    {

    }
}