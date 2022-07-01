package de.jagenka.timer

import de.jagenka.Coordinates
import de.jagenka.DeathGames
import de.jagenka.Util.teleport
import de.jagenka.config.Config
import de.jagenka.managers.PlayerManager
import de.jagenka.team.TeamSelectorUI
import de.jagenka.toDGCoordinates
import net.minecraft.world.GameMode

object BasicTpTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = false
    override val runEvery: Int
        get() = 1.ticks()

    override fun run()
    {
        if (!DeathGames.running)
        {
            PlayerManager.getOnlinePlayers().forEach { player ->
                if (!player.interactionManager.isSurvivalLike)
                {
                    if (!player.hasPermissionLevel(2)) player.changeGameMode(GameMode.ADVENTURE)

                    //tp spectators back to arena
                    val (posX, negX) = listOf(Config.arenaBounds.secondCorner.x, Config.arenaBounds.firstCorner.x).sortedDescending()
                    val (posZ, negZ) = listOf(Config.arenaBounds.secondCorner.z, Config.arenaBounds.firstCorner.z).sortedDescending()
                    if (player.pos.x > posX + Config.spectatorRadiusPadding) player.teleport(Coordinates(posX.toDouble(), player.pos.y, player.pos.z, player.yaw, player.pitch))
                    if (player.pos.x < negX - Config.spectatorRadiusPadding) player.teleport(Coordinates(negX.toDouble(), player.pos.y, player.pos.z, player.yaw, player.pitch))
                    if (player.pos.z > posZ + Config.spectatorRadiusPadding) player.teleport(Coordinates(player.pos.x, player.pos.y, posZ.toDouble(), player.yaw, player.pitch))
                    if (player.pos.z < negZ - Config.spectatorRadiusPadding) player.teleport(Coordinates(player.pos.x, player.pos.y, negZ.toDouble(), player.yaw, player.pitch))
                    return@forEach
                }
                if (!TeamSelectorUI.lobbyBounds.contains(player.pos)) player.teleport(Config.lobbySpawn)
            }
        }
    }

    override fun reset()
    {

    }
}