package de.jagenka.timer

import de.jagenka.DeathGames
import de.jagenka.config.Config
import de.jagenka.managers.PlayerManager
import de.jagenka.managers.PlayerManager.isOp
import de.jagenka.util.surroundingBlockPos
import de.jagenka.util.teleportTo
import de.jagenka.util.withRotation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.Vec3

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
                    if (!Config.general.lobbyBounds.isInside(player.position().surroundingBlockPos()))
                    {
                        player.teleportTo(Config.spawns.lobbySpawn)
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
        val maxX = Config.general.arenaBounds.maxX()
        val minX = Config.general.arenaBounds.minX()
        val maxZ = Config.general.arenaBounds.maxZ()
        val minZ = Config.general.arenaBounds.minZ()

        if (player.position().x > maxX + Config.general.spectatorRadiusPadding) player.teleportTo(
            Vec3(
                maxX.toDouble(),
                player.position().y,
                player.position().z
            ).withRotation(player.yRot, player.xRot)
        )
        if (player.position().x < minX - Config.general.spectatorRadiusPadding) player.teleportTo(
            Vec3(
                minX.toDouble(),
                player.position().y,
                player.position().z
            ).withRotation(player.yRot, player.xRot)
        )
        if (player.position().z > maxZ + Config.general.spectatorRadiusPadding) player.teleportTo(
            Vec3(
                player.position().x,
                player.position().y,
                maxZ.toDouble()
            ).withRotation(player.yRot, player.xRot)
        )
        if (player.position().z < minZ - Config.general.spectatorRadiusPadding) player.teleportTo(
            Vec3(
                player.position().x,
                player.position().y,
                minZ.toDouble()
            ).withRotation(player.yRot, player.xRot)
        )
    }

    override fun reset()
    {

    }
}