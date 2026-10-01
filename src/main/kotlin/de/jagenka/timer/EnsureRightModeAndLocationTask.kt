package de.jagenka.timer

import de.jagenka.Coordinates
import de.jagenka.DeathGames
import de.jagenka.Util.teleport
import de.jagenka.config.Config
import de.jagenka.managers.PlayerManager
import de.jagenka.managers.PlayerManager.isOp
import de.jagenka.util.cuboidBigCorner
import de.jagenka.util.cuboidContains
import de.jagenka.util.cuboidSmallCorner
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameType

object EnsureRightModeAndLocationTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = false
    override val isGameMechanic: Boolean
        get() = false
    override val runEvery: Int
        get() = 1.ticks()

    override fun run()
    {

        PlayerManager.getOnlinePlayers().forEach { player ->
            // everyone needs to stay in arena, at all times
            keepInArenaBounds(player)

            if (!DeathGames.running)
            {
                // This checks Survival OR Adventure mode
                if (!player.gameMode.isSurvival)
                {
                    // Operators are allowed to be in Creative or Spectator and outside of lobby bounds.
                    if (!player.isOp())
                    {
                        player.setGameMode(GameType.ADVENTURE)
                    }
                }

                if (!player.isOp())
                {
                    //this keeps normies in lobby, when game is not running
                    if (!Config.general.lobbyBounds.cuboidContains(player.position()))
                    {
                        player.teleport(Config.spawns.lobbySpawn)
                    }
                }
            }
        }
    }

    /**
     * TP a player back to arena, if sufficiently outside.
     */
    private fun keepInArenaBounds(player: ServerPlayer)
    {
        val (posX, negX) = listOf(Config.general.arenaBounds.cuboidBigCorner().x, Config.general.arenaBounds.cuboidSmallCorner().x).sortedDescending()
        val (posZ, negZ) = listOf(Config.general.arenaBounds.cuboidBigCorner().z, Config.general.arenaBounds.cuboidSmallCorner().z).sortedDescending()
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
    }

    override fun reset()
    {

    }
}