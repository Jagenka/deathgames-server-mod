package de.jagenka.util

import de.jagenka.DeathGames
import de.jagenka.config.Config
import de.jagenka.managers.SpawnManager
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.AABB

object LobbyUtil
{
    /**
     * @return if tp was successful
     */
    fun teleportToLobby(player: ServerPlayer): Boolean
    {
        val lobbySpawn = Config.spawns.lobbySpawn
        val lobbyBounds = Config.general.lobbyBounds
        val shopCenters = Config.shopSettings.shopBounds.map { AABB.of(it).center }

        // if tp successful, we're done. else continue
        if (player.teleportTo(lobbySpawn))
        {
            return true
        }
        if (lobbyBounds != null &&
            player.teleportTo(lobbyBounds.findTpDestinationInside(player.level()))
        )
        {
            return true
        }

        if (player.teleportTo(
                SpawnManager.findTpLocationsAround(
                    player.level(), shopCenters
                ).randomOrNull()
            )
        )
        {
            return true
        }

        // worst case, should never happen
        DeathGames.logger.error("error finding lobby tp position")
        return false
    }

    /**
     * @return `true`/`false` if bounds exist and player is/not in lobby bounds, `null` if bounds do not exist
     */
    fun isInLobbyBounds(player: ServerPlayer): Boolean?
    {
        val lobbyBounds = Config.general.lobbyBounds
        if (lobbyBounds === null) return null
        return lobbyBounds.isInside(player.blockPosition())
    }
}