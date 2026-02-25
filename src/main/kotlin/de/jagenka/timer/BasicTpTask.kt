package de.jagenka.timer

import de.jagenka.Coordinates
import de.jagenka.DeathGames
import de.jagenka.Util.teleport
import de.jagenka.config.Config
import de.jagenka.managers.PlayerManager
import de.jagenka.managers.PlayerManager.isOp
import de.jagenka.team.TeamSelectorUI
import net.minecraft.world.level.GameType

object BasicTpTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = false
    override val isGameMechanic: Boolean
        get() = false
    override val runEvery: Int
        get() = 1.ticks()

    override fun run()
    {
        if (!DeathGames.running)
        {
            PlayerManager.getOnlinePlayers().forEach { player ->
                if (!player.gameMode.isSurvival)
                {
                    if (!player.isOp()) player.setGameMode(GameType.ADVENTURE)

                    //tp spectators back to arena
                    val (posX, negX) = listOf(Config.general.arenaBounds.secondCorner.x, Config.general.arenaBounds.firstCorner.x).sortedDescending()
                    val (posZ, negZ) = listOf(Config.general.arenaBounds.secondCorner.z, Config.general.arenaBounds.firstCorner.z).sortedDescending()
                    if (player.position().x > posX + Config.general.spectatorRadiusPadding) player.teleport(
                        Coordinates(
                            posX.toDouble(),
                            player.position().y,
                            player.position().z,
                            player.yRot,
                            player.xRot
                        )
                    )
                    if (player.position().x < negX - Config.general.spectatorRadiusPadding) player.teleport(
                        Coordinates(
                            negX.toDouble(),
                            player.position().y,
                            player.position().z,
                            player.yRot,
                            player.xRot
                        )
                    )
                    if (player.position().z > posZ + Config.general.spectatorRadiusPadding) player.teleport(
                        Coordinates(
                            player.position().x,
                            player.position().y,
                            posZ.toDouble(),
                            player.yRot,
                            player.xRot
                        )
                    )
                    if (player.position().z < negZ - Config.general.spectatorRadiusPadding) player.teleport(
                        Coordinates(
                            player.position().x,
                            player.position().y,
                            negZ.toDouble(),
                            player.yRot,
                            player.xRot
                        )
                    )
                    return@forEach
                }
                if (!TeamSelectorUI.lobbyBounds.contains(player.position())) player.teleport(Config.spawns.lobbySpawn)
            }
        }
    }

    override fun reset()
    {

    }
}