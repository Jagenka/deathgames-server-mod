package de.jagenka.managers

import de.jagenka.BlockCuboid
import de.jagenka.Coordinates
import de.jagenka.DeathGames
import de.jagenka.Util
import de.jagenka.Util.teleport
import de.jagenka.config.Config
import de.jagenka.config.Config.defaultSpawn
import de.jagenka.managers.DisplayManager.sendChatMessage
import de.jagenka.team.DGTeam
import de.jagenka.team.isDGColorBlock
import de.jagenka.util.BiMap
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.GameMode
import de.jagenka.config.Config.spawnPlatformRadius as platformRadius

object SpawnManager
{
    val spawns
        get() = Config.configEntry.spawns.spawnPositions.coords.map { DGSpawn(it) }

    private val teamSpawns = BiMap<DGSpawn, DGTeam>()

    fun getTeam(spawn: DGSpawn) = teamSpawns[spawn]

    fun ServerPlayerEntity.getSpawnCoordinates(): Coordinates
    {
        return PlayerManager.getTeam(this)?.let { team ->
            teamSpawns.getKeyForValue(team)?.coordinates
        } ?: defaultSpawn
    }

    fun teleportPlayerToSpawn(player: ServerPlayerEntity)
    {
        val spawnCoordinates = player.getSpawnCoordinates()
        player.teleport(spawnCoordinates)
        player.yaw = spawnCoordinates.yaw
        if (spawnCoordinates == defaultSpawn) player.changeGameMode(GameMode.SPECTATOR)
    }

    fun shuffleSpawns()
    {
        shuffleSpawns(PlayerManager.getNonEmptyTeams().toList())
    }

    fun shuffleSpawns(teams: List<DGTeam>)
    {
        check(teams.size <= spawns.size)

        teamSpawns.clear()

        teams.forEach { team ->
            teamSpawns[getUnassignedSpawns().random()] = team
        }

        colorSpawns()

        if (DeathGames.running) sendChatMessage("Spawns shuffled!")
    }

    fun colorSpawns()
    {
        spawns.forEach { spawn ->
            val team = teamSpawns[spawn]
            if (team == null)
            {
                Util.getBlocksInSquareRadiusAtFixY(spawn.coordinates.asBlockPos().relative(0, -1, 0), platformRadius).forEach { (block, coordinates) ->
                    if (block.isDGColorBlock()) Util.setBlockAt(coordinates, DGTeam.defaultColorBlock)
                }
            } else
            {
                Util.getBlocksInSquareRadiusAtFixY(spawn.coordinates.asBlockPos().relative(0, -1, 0), platformRadius).forEach { (block, coordinates) ->
                    if (block.isDGColorBlock()) Util.setBlockAt(coordinates, team.getColorBlock())
                }
            }
        }
    }

    fun resetSpawnColoring()
    {
        spawns.forEach { (coordinates) ->
            Util.getBlocksInSquareRadiusAtFixY(coordinates.asBlockPos().relative(0, -1, 0), platformRadius).forEach { (block, coordinates) ->
                if (block.isDGColorBlock()) Util.setBlockAt(coordinates, DGTeam.defaultColorBlock)
            }
        }
    }

    fun getUnassignedSpawns() = spawns.filter { teamSpawns[it] == null }.toList()

    /**
     * @return who owned the spawn before
     */
    fun reassignSpawn(spawn: DGSpawn, team: DGTeam): DGTeam?
    {
        teamSpawns.removeForValue(team)

        val teamPreviouslyAssignedToSpawn = getTeam(spawn)
        if (teamPreviouslyAssignedToSpawn != null)
        {
            teamSpawns[getUnassignedSpawns().random()] = teamPreviouslyAssignedToSpawn
        }

        teamSpawns[spawn] = team

        colorSpawns()

        return teamPreviouslyAssignedToSpawn
    }
}

data class DGSpawn(val coordinates: Coordinates)
{
    fun getCuboid() = BlockCuboid(coordinates.asBlockPos().relative(-platformRadius, 0, -platformRadius), coordinates.asBlockPos().relative(platformRadius, 2, platformRadius))

    fun containsPlayer(player: ServerPlayerEntity) = getCuboid().contains(player.pos)
}