package de.jagenka

import de.jagenka.Util.ifServerLoaded
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Formatting

object DGPlayerManager
{
    private val players = mutableSetOf<ServerPlayerEntity>()
    private val teamRegistry = mutableMapOf<ServerPlayerEntity, DGTeam>()
    private val inGame = mutableListOf<ServerPlayerEntity>()

    fun getPlayer(name: String): ServerPlayerEntity?
    {
        players.forEach { player -> if (player.name.asString() == name) return player }

        var result: ServerPlayerEntity? = null
        ifServerLoaded { server -> server.playerManager.playerList.forEach { player -> if (player.name.asString() == name) result = player } }
        if (result != null) players.add(result!!)
        return result
    }

    fun getTeam(player: ServerPlayerEntity) = teamRegistry[player]

    fun ServerPlayerEntity.getDGTeam() = getTeam(this)

    fun registerPlayerToTeam(player: ServerPlayerEntity, team: DGTeam)
    {
        teamRegistry[player] = team
        ifServerLoaded { it.scoreboard.addPlayerToTeam(player.name.asString(), it.scoreboard.getTeam(team.name)) }
    }

    fun ServerPlayerEntity.registerToTeam(team: DGTeam)
    {
        registerPlayerToTeam(this, team)
    }

    fun getNonEmptyTeams(): Set<DGTeam>
    {
        return teamRegistry.values.toHashSet()
    }

    fun reset()
    {
        prepareTeams()
        players.clear()
    }

    fun prepareTeams()
    {
        ifServerLoaded { server ->
            server.scoreboard.teams.toList().forEach { team -> server.scoreboard.removeTeam(team) }
            DGTeam.values().forEach { color -> server.scoreboard.addTeam(color.name) }
            server.scoreboard.teams.forEach { it.color = Formatting.byName(it.name.lowercase()) }
        }
    }

    fun getPlayersInTeam(team: DGTeam): List<ServerPlayerEntity>
    {
        return teamRegistry.keys.filter { teamRegistry[it] == team }
    }

    fun getInGamePlayers(): List<ServerPlayerEntity> = inGame

    fun getInGamePlayersInTeam(team: DGTeam): List<ServerPlayerEntity> = inGame.filter { teamRegistry[it] == team }

    fun ServerPlayerEntity.isInGame() = inGame.contains(this)

    fun eliminatePlayer(player: ServerPlayerEntity)
    {
        inGame.remove(player)
    }

    fun ServerPlayerEntity.eliminate()
    {
        eliminatePlayer(this)
    }

    fun getInGameTeams() = DGTeam.values().filter { it.getInGamePlayers().isNotEmpty() }
}

enum class DGTeam
{
    BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW;

    fun getPlayers() = DGPlayerManager.getPlayersInTeam(this)

    fun getInGamePlayers() = DGPlayerManager.getInGamePlayersInTeam(this)
}

data class PlayerTeamEntry(val player: ServerPlayerEntity, val team: DGTeam)
