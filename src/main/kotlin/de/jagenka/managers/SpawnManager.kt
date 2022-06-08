package de.jagenka.managers

import de.jagenka.*
import de.jagenka.Util.teleport
import de.jagenka.config.Config.defaultSpawn
import de.jagenka.config.Config.spawnPlatformRadius
import de.jagenka.managers.DisplayManager.sendChatMessage
import de.jagenka.team.DGTeam
import de.jagenka.team.isDGColorBlock
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.GameMode

object SpawnManager
{
    private val spawns = ArrayList<Coordinates>()
    private val teamSpawns = mutableMapOf<DGTeam?, Coordinates>().withDefault { defaultSpawn }

    private fun addSpawns(spawns: Collection<Coordinates>)
    {
        SpawnManager.spawns.addAll(spawns)
    }

    internal fun setSpawns(spawns: Collection<Coordinates>)
    {
        SpawnManager.spawns.clear()
        addSpawns(spawns)
    }

    private fun getSpawn(team: DGTeam?) = teamSpawns.getValue(team)

    fun getSpawns() = spawns.toList()

    fun ServerPlayerEntity.getSpawn() = getSpawn(PlayerManager.getTeam(this))

    fun teleportPlayerToSpawn(player: ServerPlayerEntity)
    {
        val spawn = player.getSpawn()
        player.teleport(spawn)
        player.yaw = spawn.yaw
        if (spawn == defaultSpawn) player.changeGameMode(GameMode.SPECTATOR)
    }

    fun shuffleSpawns()
    {
        spawns.forEach { coordinates ->
            Util.getBlocksInSquareRadiusAtFixY(coordinates.relative(0, -1, 0), spawnPlatformRadius).forEach { (block, coordinates) ->
                if (block.isDGColorBlock()) Util.setBlockAt(coordinates, DGTeam.defaultColorBlock)
            }
        }
        shuffleSpawns(PlayerManager.getNonEmptyTeams())
    }

    private fun shuffleSpawns(teams: Collection<DGTeam>)
    {
        val shuffledSpawns = spawns.shuffled()
        teamSpawns.clear()
        teams.forEachIndexed { index, team ->
            if (index >= shuffledSpawns.size) return
            teamSpawns[team] = shuffledSpawns[index]
            colorTeamSpawn(team)
        }

        if (DeathGames.running) sendChatMessage("Spawns shuffled!")
    }

    fun colorTeamSpawn(team: DGTeam)
    {
        teamSpawns[team]?.let { coordinates ->
            Util.getBlocksInSquareRadiusAtFixY(coordinates.relative(0, -1, 0), spawnPlatformRadius).forEach { (block, coordinates) ->
                if (block.isDGColorBlock()) Util.setBlockAt(coordinates, team.getColorBlock())
            }
        }
    }
}