package de.jagenka.managers

import com.mojang.brigadier.StringReader
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
import net.minecraft.commands.arguments.CompoundTagArgument
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameType

object SpawnManager
{
    private val compoundTagArgument = CompoundTagArgument.compoundTag()

    val defaultSpawn
        get() = Config.spawns.spectatorSpawn
    val platformRadius
        get() = Config.spawns.platformRadius

    val respawnEffects: List<MobEffectInstance>
        get() = compoundTagArgument.parse(
            StringReader(
                Config.spawns.respawnEffectNBTs.joinToString(
                    separator = ",", prefix = "{effects:[", postfix = "]}"
                )
            )
        ).read("effects", MobEffectInstance.CODEC.listOf()).orElse(emptyList())


    val respawnItems: List<ItemStack>
        get() = Config.spawns.respawnItems.map { (id, amount, components) ->
            Util.parseItemStack(id, components, amount)
        }

    val spawns
        get() = Config.spawns.spawnPositions.toList()

    private val teamSpawns = BiMap<DGSpawn, DGTeam>()

    fun getTeam(spawn: DGSpawn) = teamSpawns[spawn]

    fun ServerPlayer.getSpawnCoordinates(): Coordinates
    {
        return PlayerManager.getTeam(this)?.let { team ->
            teamSpawns.getKeyForValue(team)?.coordinates
        } ?: defaultSpawn
    }

    /**
     * teleports player to their spawn, and adds respawn effects and items, if player is participating (not spectator)
     */
    fun spawnPlayer(player: ServerPlayer, giveItems: Boolean = true)
    {
        // handle position
        val spawnCoordinates = player.getSpawnCoordinates()
        player.teleport(spawnCoordinates)
        player.yRot = spawnCoordinates.yaw

        // check if spectator or player
        if (spawnCoordinates == defaultSpawn)
        {
            player.setGameMode(GameType.SPECTATOR)
        } else
        {
            // handle respawn effects/items for participating players only
            player.removeAllEffects()
            applyRespawnEffects(player)

            if (giveItems)
            {
                giveRespawnItems(player)
            }
        }
    }

    fun giveRespawnItems(player: ServerPlayer)
    {
        respawnItems.forEach {
            player.addItem(it.copy())
        }
    }

    fun applyRespawnEffects(player: ServerPlayer)
    {
        respawnEffects.forEach {
            player.addEffect(MobEffectInstance(it))
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
                Util.getBlocksInSquareRadiusAtFixY(spawn.coordinates.asBlockPos().relative(0, -1, 0), platformRadius)
                    .forEach { (block, coordinates) ->
                        if (block.isDGColorBlock())
                        {
                            Util.setBlockAt(PlayerManager.getMapLevel(), coordinates, DGTeam.defaultColorBlock)
                        }
                    }
            } else
            {
                Util.getBlocksInSquareRadiusAtFixY(spawn.coordinates.asBlockPos().relative(0, -1, 0), platformRadius)
                    .forEach { (block, coordinates) ->
                        if (block.isDGColorBlock())
                        {
                            Util.setBlockAt(PlayerManager.getMapLevel(), coordinates, team.getColorBlock())
                        }
                    }
            }
        }
    }

    fun resetSpawnColoring()
    {
        spawns.forEach { (coordinates) ->
            Util.getBlocksInSquareRadiusAtFixY(coordinates.asBlockPos().relative(0, -1, 0), platformRadius)
                .forEach { (block, coordinates) ->
                    if (block.isDGColorBlock())
                    {
                        Util.setBlockAt(PlayerManager.getMapLevel(), coordinates, DGTeam.defaultColorBlock)
                    }
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
    fun getCuboid() = BlockCuboid(
        coordinates.asBlockPos().relative(-platformRadius, 0, -platformRadius),
        coordinates.asBlockPos().relative(platformRadius, 2, platformRadius)
    )

    fun containsPlayer(player: ServerPlayer) = getCuboid().contains(player.position())
}