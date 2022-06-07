package de.jagenka.managers

import de.jagenka.Coordinates
import de.jagenka.DGTeam
import de.jagenka.Util.ifServerLoaded
import de.jagenka.timer.seconds
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Formatting
import net.minecraft.world.GameMode

object PlayerManager
{
    private val playerNames = mutableSetOf<String>()
    private val inGameMap = mutableMapOf<String, Boolean>().withDefault { false } // TODO participating
    private val teamRegistry = mutableMapOf<String, DGTeam>()

    fun getOnlinePlayer(name: String): ServerPlayerEntity? = getOnlinePlayers().find { it.name.asString() == name }

    fun getOnlinePlayers(): Set<ServerPlayerEntity>
    {
        val allPlayers = mutableSetOf<ServerPlayerEntity>()
        ifServerLoaded { allPlayers.addAll(it.playerManager.playerList) }
        return allPlayers.toSet()
    }

    fun getOnlineInGamePlayers() = getOnlinePlayers().filter { inGameMap.getValue(it.name.asString()) }

    fun getPlayers(): Set<String>
    {
        ifServerLoaded { server ->
            server.playerManager.playerList.forEach {
                if (!playerNames.contains(it.name.asString())) playerNames.add(it.name.asString())
            }
        }

        return playerNames.toSet()
    }

    fun getTeam(player: ServerPlayerEntity) = getTeam(player.name.asString())
    fun getTeam(playerName: String) = teamRegistry[playerName]

    fun ServerPlayerEntity.getDGTeam() = getTeam(this)

    fun ServerPlayerEntity.addToDGTeam(team: DGTeam) = addPlayerToTeam(this, team)
    fun addPlayerToTeam(player: ServerPlayerEntity, team: DGTeam)
    {
        ifServerLoaded {
            it.scoreboard.addPlayerToTeam(player.name.asString(), it.scoreboard.getTeam(team.name))
            teamRegistry[player.name.asString()] = team
        }
    }

    fun ServerPlayerEntity.kickFromDGTeam() = kickPlayerFromTeam(this)
    fun kickPlayerFromTeam(player: ServerPlayerEntity)
    {
        ifServerLoaded {
            it.scoreboard.clearPlayerTeam(player.name.asString())
            teamRegistry.remove(player.name.asString())
        }
    }

    fun getNonEmptyTeams(): Set<DGTeam>
    {
        return teamRegistry.values.toHashSet()
    }

    fun reset()
    {

    }

    fun prepareTeams()
    {
        ifServerLoaded { server ->
            DGTeam.values().forEach { color ->
                server.scoreboard.addTeam(color.name)
                server.scoreboard.getTeam(color.name)?.color = Formatting.byName(color.name.lowercase())
            }
        }
    }

    fun getTeamPlayers() = getOnlinePlayers().filter { teamRegistry[it.name.asString()] != null }

    fun getPlayersInTeam(team: DGTeam): List<String>
    {
        return teamRegistry.keys.filter { teamRegistry[it] == team }
    }

    fun getOnlinePlayersInTeam(team: DGTeam): List<ServerPlayerEntity>
    {
        return getOnlinePlayers().filter { it.getDGTeam() == team }
    }

    fun getInGamePlayers(): List<String>
    {
        return playerNames.filter { inGameMap.getValue(it) }.toList()
    }

    fun getInGamePlayersInTeam(team: DGTeam): List<String> = getInGamePlayers().filter { teamRegistry[it] == team }

    fun ServerPlayerEntity.isInGame() = getInGamePlayers().contains(this.name.asString())


    fun ServerPlayerEntity.makeInGame()
    {
        inGameMap[this.name.asString()] = true
    }

    fun ServerPlayerEntity.eliminate()
    {
        inGameMap[this.name.asString()] = false
        this.changeGameMode(GameMode.SPECTATOR)
    }

    fun getInGameTeams() = DGTeam.values().filter { getInGamePlayersInTeam(it).isNotEmpty() }
    fun getOnlineInGameTeams() = DGTeam.values().filter { it.getOnlineInGamePlayers().isNotEmpty() }

    fun Coordinates.getInGamePlayersInRange(range: Double) = getOnlinePlayersInRange(range).filter { player ->
        inGameMap.getValue(player.name.asString())
    }

    fun Coordinates.getOnlinePlayersInRange(range: Double) = getOnlinePlayers().filter { player ->
        this distanceTo player.pos <= range
    }

    @JvmStatic
    fun onPlayerJoin(player: ServerPlayerEntity)
    {
        if (player.getDGTeam() == null)
        {
            ifServerLoaded { server -> server.scoreboard.clearPlayerTeam(player.name.asString()) }
        }

        DisplayManager.updateLevelDisplay()
    }

    @JvmStatic
    fun handleRespawn(player: ServerPlayerEntity)
    {
        SpawnManager.teleportPlayerToSpawn(player)
        player.addStatusEffect(StatusEffectInstance(StatusEffects.RESISTANCE, 5.seconds(), 255))
    }
}
