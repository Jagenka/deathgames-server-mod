package de.jagenka.managers

import com.mojang.brigadier.StringReader
import de.jagenka.DeathGames
import de.jagenka.Util
import de.jagenka.config.Config
import de.jagenka.managers.DisplayManager.sendChatMessage
import de.jagenka.managers.PlayerManager.getDGTeam
import de.jagenka.managers.SpawnManager.platformRadius
import de.jagenka.team.DGTeam
import de.jagenka.team.isDGColorBlock
import de.jagenka.util.BiMap
import de.jagenka.util.center
import de.jagenka.util.surroundingBlockPos
import de.jagenka.util.teleportTo
import kotlinx.serialization.Serializable
import net.minecraft.commands.arguments.CompoundTagArgument
import net.minecraft.core.BlockPos
import net.minecraft.core.PositionAndRotation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.BlockTags
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.GameType
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.phys.Vec3
import kotlin.math.max
import kotlin.random.Random

object SpawnManager
{
    private val compoundTagArgument = CompoundTagArgument.compoundTag()

    val spectatorSpawn
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

    val spawnRandomlyNearBonus: Boolean
        get() = true // TODO: move to config

    fun getTeam(spawn: DGSpawn) = teamSpawns[spawn]

    /**
     * teleports player to their spawn, and adds respawn effects if player is participating (not spectator)
     */
    fun teleportPlayerToSpawn(player: ServerPlayer)
    {
        // handle position
        val spawnCoordinates = getSpawnCoordinates(player)

        player.teleportTo(spawnCoordinates)

        // check if spectator or player
        if (spawnCoordinates == spectatorSpawn)
        {
            player.setGameMode(GameType.SPECTATOR)
        } else
        {
            // handle respawn effects/items for participating players only
            player.removeAllEffects()
            applyRespawnEffects(player)
        }
    }

    private fun getSpawnCoordinates(player: ServerPlayer): PositionAndRotation // TODO: gets called twice on game start? maybe?
    {
        // players without team must be spectators
        if (player.getDGTeam() == null) return spectatorSpawn

        if (spawnRandomlyNearBonus) // ignore spawn locations from config and find spawn near bonus
        {
            // idea: spawn as near as possible to a team member, but as far away as possible to enemies. apply min and max radius, make sure to intersect with arena bounds

            val minRadiusFromBonus = 50.0 // TODO: move to config
            val maxRadiusFromBonus = 100.0 // if possible TODO: move to config
            val tries = 1000

            val level = player.level()
            val selectedPlatforms = BonusManager.getSelectedPlatforms() // TODO: what to do if empty, especially when bonus is disabled lol

            // find potential spawn locations:
            val possibleTargets = 0.rangeUntil(tries).mapNotNull {
                selectedPlatforms.randomOrNull()?.let { platform ->
                    val dist = Random.nextDouble(minRadiusFromBonus, maxRadiusFromBonus)
                    val pitch = Random.nextDouble(-45.0, 45.0)
                    val yaw = Random.nextDouble(-180.0, 180.0)

                    //DeathGames.logger.info("$dist, $pitch, $yaw")

                    val target = BlockPos.containing(
                        Vec3.atCenterOf(platform.pos.above())
                            .add(Vec3.directionFromRotation(pitch.toFloat(), yaw.toFloat()).normalize().scale(dist))
                    )

                    // TODO: check if in arena bounds

                    //DeathGames.logger.info("$target")

                    val finalPos = findNearestSpawnLocationOnYAxis(level, target)

                    return@mapNotNull if (finalPos != null && Config.general.arenaBounds.isInside(finalPos)) finalPos else null
                }
            }
            // TODO: fallback? need at least one position

            val (teamMembers, enemies) = PlayerManager.getOnlineParticipatingPlayers().partition { it.team == player.team }

            val sortedResults = possibleTargets
                .map { Vec3.atBottomCenterOf(it) }
                .map { pos ->
                    // when there are now enemies found, this position is perfect
                    val enemyDist = enemies.minOfOrNull { it.position().distanceToSqr(pos) } ?: Double.MAX_VALUE
                    // when no mates are found, just look an enemy distance
                    val teamDist = teamMembers.minOfOrNull { it.position().distanceToSqr(pos) } ?: 0.0 // TODO: weigh this more?
                    return@map pos to (enemyDist - teamDist)
                }
                .sortedByDescending { it.second }
                .map { pair -> // first: tp position, second: viability score
                    val rotation = pair.first.vectorTo(
                        selectedPlatforms.minBy { it.pos.center().distanceTo(pair.first) }.pos.above().center()
                    ).rotation()
                    return@map PositionAndRotation.of(pair.first, rotation.y, rotation.x) // TODO: this right?
                }

            return sortedResults.firstOrNull() ?: spectatorSpawn

        } else // default behavior: pre-set spawn locations
        {
            return PlayerManager.getTeam(player)?.let { team ->
                teamSpawns.getKeyForValue(team)?.positionAndRotation
            } ?: spectatorSpawn
        }
    }

    private fun findNearestSpawnLocationOnYAxis(level: BlockGetter, pos: BlockPos): BlockPos?
    {
        var isAir1: Boolean
        var isAir2: Boolean
        var isGroundSolid: Boolean

        for (i in 0.rangeTo(max(level.maxY - pos.y, pos.y)))
        {
            // up
            isAir2 = level.getBlockState(pos.offset(0, i + 1, 0)).isAir
            isAir1 = level.getBlockState(pos.offset(0, i, 0)).isAir
            var groundBlockState = level.getBlockState(pos.offset(0, i - 1, 0))
            isGroundSolid = !groundBlockState.isAir && !groundBlockState.liquid() && groundBlockState.`is`(BlockTags.ENTITIES_CAN_TELEPORT_TO)

            if (isAir1 && isAir2 && isGroundSolid) return pos.offset(0, i, 0)

            // down
            isAir2 = level.getBlockState(pos.offset(0, -i + 1, 0)).isAir
            isAir1 = level.getBlockState(pos.offset(0, -i, 0)).isAir
            groundBlockState = level.getBlockState(pos.offset(0, -i - 1, 0))
            isGroundSolid = !groundBlockState.isAir && !groundBlockState.liquid() && groundBlockState.`is`(BlockTags.ENTITIES_CAN_TELEPORT_TO)

            if (isAir1 && isAir2 && isGroundSolid) return pos.offset(0, -i, 0)
        }

        return null
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
                Util.getBlocksInSquareRadiusAtFixY(spawn.positionAndRotation.position().surroundingBlockPos().offset(0, -1, 0), platformRadius)
                    .forEach { (block, coordinates) ->
                        if (block.isDGColorBlock())
                        {
                            Util.setBlockAt(PlayerManager.getMapLevel(), coordinates, DGTeam.defaultColorBlock)
                        }
                    }
            } else
            {
                Util.getBlocksInSquareRadiusAtFixY(spawn.positionAndRotation.position().surroundingBlockPos().offset(0, -1, 0), platformRadius)
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
        spawns.forEach { (positionAndRotation) ->
            Util.getBlocksInSquareRadiusAtFixY(
                positionAndRotation.position().surroundingBlockPos().offset(0, -1, 0), platformRadius
            )
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
data class DGSpawn(val positionAndRotation: PositionAndRotation, val defaultOwner: DGTeam?)
{
    fun getBoundingBox(): BoundingBox = BoundingBox.fromCorners(
        positionAndRotation.position().surroundingBlockPos().offset(-platformRadius, 0, -platformRadius),
        positionAndRotation.position().surroundingBlockPos().offset(platformRadius, 2, platformRadius)
    )

    fun containsPlayer(player: ServerPlayer) = getBoundingBox().isInside(player.position().surroundingBlockPos()) // TODO: does this work?
}