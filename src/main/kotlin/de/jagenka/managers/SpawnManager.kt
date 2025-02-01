package de.jagenka.managers

import de.jagenka.BlockCuboid
import de.jagenka.Coordinates
import de.jagenka.DeathGames
import de.jagenka.Util
import de.jagenka.Util.teleport
import de.jagenka.config.Config
import de.jagenka.managers.DisplayManager.sendChatMessage
import de.jagenka.managers.SpawnManager.platformRadius
import de.jagenka.team.DGTeam
import de.jagenka.team.isDGColorBlock
import de.jagenka.util.BiMap
import kotlinx.serialization.Serializable
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.nbt.StringNbtReader
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.GameMode

object SpawnManager
{
    val defaultSpawn
        get() = Config.spawns.spectatorSpawn
    val platformRadius
        get() = Config.spawns.platformRadius

    val respawnEffects = Config.spawns.respawnEffectNBTs.mapNotNull {
        StatusEffectInstance.fromNbt(StringNbtReader.parse(it))
    }

    val respawnItems = Config.spawns.respawnItems.map { (id, amount, components) ->
        Util.parseItemStack(id, components, amount)
    }

    val spawns
        get() = Config.spawns.spawnPositions.toList()

    private val teamSpawns = BiMap<DGSpawn, DGTeam>()

    fun getTeam(spawn: DGSpawn) = teamSpawns[spawn]

    fun ServerPlayerEntity.getSpawnCoordinates(): Coordinates
    {
        return PlayerManager.getTeam(this)?.let { team ->
            teamSpawns.getKeyForValue(team)?.coordinates
        } ?: defaultSpawn
    }

    /**
     * teleports player to their spawn, and adds respawn effects and items, if player is participating (not spectator)
     */
    fun spawnPlayer(player: ServerPlayerEntity)
    {
        // handle position
        val spawnCoordinates = player.getSpawnCoordinates()
        player.teleport(spawnCoordinates)
        player.yaw = spawnCoordinates.yaw

        // check if spectator or player
        if (spawnCoordinates == defaultSpawn)
        {
            player.changeGameMode(GameMode.SPECTATOR)
        } else
        {
            // handle respawn effects/items for participating players only
            player.clearStatusEffects()
            applyRespawnEffects(player)

            respawnItems.forEach {
                player.giveItemStack(it.copy())
            }
        }
    }

    fun applyRespawnEffects(player: ServerPlayerEntity)
    {
        respawnEffects.forEach {
            player.addStatusEffect(StatusEffectInstance(it))
        }
    }

    fun initSpawns()
    {
        if (Config.spawns.enableShuffle)
        {
            shuffleSpawns()
        } else
        {
            val teamsWithoutSpawn = mutableListOf<DGTeam>()

            PlayerManager.getNonEmptyTeams().toList().forEach { participatingTeam ->
                val spawn = spawns.find { it.defaultOwner == participatingTeam }
                if (spawn != null) teamSpawns[spawn] = participatingTeam
                else teamsWithoutSpawn.add(participatingTeam)
            }

            teamsWithoutSpawn.forEach { teamSpawns[getUnassignedSpawns().random()] = it }

            colorSpawns()
        }
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

@Serializable
data class DGSpawn(val coordinates: Coordinates, val defaultOwner: DGTeam?)
{
    fun getCuboid() = BlockCuboid(coordinates.asBlockPos().relative(-platformRadius, 0, -platformRadius), coordinates.asBlockPos().relative(platformRadius, 2, platformRadius))

    fun containsPlayer(player: ServerPlayerEntity) = getCuboid().contains(player.pos)
}