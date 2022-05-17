package de.jagenka

import de.jagenka.Util.ifServerLoaded
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Formatting

object DGPlayerManager
{
    private val players = mutableSetOf<ServerPlayerEntity>()
    private val teamRegistry = mutableMapOf<ServerPlayerEntity, DGTeam>()
    private val inGame = mutableSetOf<ServerPlayerEntity>()

    fun getPlayer(name: String): ServerPlayerEntity?
    {
        getPlayers().forEach { if (it.name.asString() == name) return it }
        return null
    }

    fun getPlayers(): Set<ServerPlayerEntity>
    {
        val allPlayers = mutableSetOf<ServerPlayerEntity>()
        allPlayers.addAll(players)
        ifServerLoaded { allPlayers.addAll(it.playerManager.playerList) }
        return allPlayers
    }

    fun getTeam(player: ServerPlayerEntity) = teamRegistry[player]

    fun ServerPlayerEntity.getDGTeam() = getTeam(this)

    fun ServerPlayerEntity.registerToTeam(team: DGTeam)
    {
        ifServerLoaded {
            it.scoreboard.addPlayerToTeam(this.name.asString(), it.scoreboard.getTeam(team.name))
            teamRegistry[this] = team
        }
    }

    fun ServerPlayerEntity.clearTeam()
    {
        ifServerLoaded {
            it.scoreboard.clearPlayerTeam(this.name.asString())
            teamRegistry.remove(this)
        }
    }

    fun getNonEmptyTeams(): Set<DGTeam>
    {
        return teamRegistry.values.toHashSet()
    }

    fun reset()
    {
        players.clear()
    }

    fun prepareTeams()
    {
        ifServerLoaded { server ->
            server.scoreboard.teams.toList().forEach { team -> server.scoreboard.removeTeam(team) }
            DGTeam.values().forEach { color ->
                server.scoreboard.addTeam(color.name)
                server.scoreboard.getTeam(color.name)?.color = Formatting.byName(color.name.lowercase())
            }
        }
    }

    fun getTeamPlayers() = getPlayers().filter { teamRegistry[it] != null }

    fun getPlayersInTeam(team: DGTeam): List<ServerPlayerEntity>
    {
        return teamRegistry.keys.filter { teamRegistry[it] == team }
    }

    fun getInGamePlayers() = inGame

    fun getInGamePlayersInTeam(team: DGTeam): List<ServerPlayerEntity> = inGame.filter { teamRegistry[it] == team }

    fun ServerPlayerEntity.isInGame() = inGame.contains(this)

    fun ServerPlayerEntity.makeInGame()
    {
        inGame.add(this)
    }

    fun ServerPlayerEntity.eliminate()
    {
        inGame.remove(this)
    }

    fun getInGameTeams() = DGTeam.values().filter { it.getInGamePlayers().isNotEmpty() }
}

enum class DGTeam
{
    BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW;

    fun getPlayers() = DGPlayerManager.getPlayersInTeam(this)

    fun getInGamePlayers() = DGPlayerManager.getInGamePlayersInTeam(this)

    companion object
    {
        fun random() = values().random()
    }
}

data class PlayerTeamEntry(val player: ServerPlayerEntity, val team: DGTeam)
