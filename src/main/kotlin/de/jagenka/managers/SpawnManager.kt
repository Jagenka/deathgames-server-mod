package de.jagenka.managers

import de.jagenka.*
import de.jagenka.Util.teleport
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
    private val spawns = ArrayList<DGSpawn>()
    private val teamSpawns = BiMap<DGSpawn, DGTeam>()

    private fun addSpawns(spawns: Collection<Coordinates>)
    {
        SpawnManager.spawns.addAll(spawns.map { DGSpawn(it) })
    }

    internal fun setSpawns(spawns: Collection<Coordinates>)
    {
        SpawnManager.spawns.clear()
        addSpawns(spawns)
    }

    fun getTeam(spawn: DGSpawn) = teamSpawns[spawn]

    fun getSpawns() = spawns.toList()

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
                Util.getBlocksInSquareRadiusAtFixY(spawn.coordinates.relative(0, -1, 0), platformRadius).forEach { (block, coordinates) ->
                    if (block.isDGColorBlock()) Util.setBlockAt(coordinates, DGTeam.defaultColorBlock)
                }
            } else
            {
                Util.getBlocksInSquareRadiusAtFixY(spawn.coordinates.relative(0, -1, 0), platformRadius).forEach { (block, coordinates) ->
                    if (block.isDGColorBlock()) Util.setBlockAt(coordinates, team.getColorBlock())
                }
            }
        }
    }

    fun getUnassignedSpawns() = spawns.filter { teamSpawns[it] == null }.toList()

    fun reassignSpawn(spawn: DGSpawn, team: DGTeam)
    {
        teamSpawns.removeForValue(team)

        val teamAssignedToSpawn = getTeam(spawn)
        if (teamAssignedToSpawn != null)
        {
            teamSpawns[getUnassignedSpawns().random()] = teamAssignedToSpawn
        }

        teamSpawns[spawn] = team

        colorSpawns()

        //TODO: capture message
    }
}

data class DGSpawn(val coordinates: Coordinates)
{
    fun getCuboid() = BlockCuboid(coordinates.relative(-platformRadius, 0, -platformRadius), coordinates.relative(platformRadius, 2, platformRadius))

    fun containsPlayer(player: ServerPlayerEntity) = getCuboid().contains(player.pos)
}