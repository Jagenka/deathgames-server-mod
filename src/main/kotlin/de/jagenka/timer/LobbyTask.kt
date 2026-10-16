package de.jagenka.timer

import de.jagenka.GameState
import de.jagenka.managers.DisplayManager
import de.jagenka.managers.PlayerManager
import de.jagenka.managers.PlayerManager.isOp
import de.jagenka.team.TeamSelectorUI
import de.jagenka.util.I18n
import net.minecraft.network.chat.Component

object LobbyTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = false
    override val isGameMechanic: Boolean
        get() = false
    override val runEvery: Int
        get() = 1.ticks()

    override fun run()
    {
        if (GameState.current == GameState.RUNNING) return

        DisplayManager.resetBossBars()

        PlayerManager.getOnlinePlayers().forEach { player ->
            if (!player.isOp())
            {
                player.inventory.clearContent()
            }
            if (TeamSelectorUI.isInLobbyBounds(player))
            {
                if (GameState.current == GameState.PREPARING)
                {
                    DisplayManager.sendMessageToHotbar(Component.literal(I18n.get("openTeamUI")))
                }

                player.health = 20f //set max hearts
                player.foodData.eat(20, 1f) //set max food and saturation
            }
        }
    }

    override fun reset()
    {

    }
}