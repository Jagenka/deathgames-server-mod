package de.jagenka

import de.jagenka.Util.ifServerLoaded
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Formatting
import net.minecraft.world.GameMode

object DGPlayerManager
{
    private val playerNames = mutableSetOf<String>()
    private val inGameMap = mutableMapOf<String, Boolean>().withDefault { false }
    private val teamRegistry = mutableMapOf<String, DGTeam>()

    fun getOnlinePlayer(name: String): ServerPlayerEntity?
    {
        getOnlinePlayers().forEach { if (it.name.asString() == name) return it }
        return null
    }

    fun getOnlinePlayers(): Set<ServerPlayerEntity>
    {
        val allPlayers = mutableSetOf<ServerPlayerEntity>()
        ifServerLoaded { allPlayers.addAll(it.playerManager.playerList) }
        return allPlayers.toSet()
    }

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

    fun ServerPlayerEntity.registerToTeam(team: DGTeam)
    {
        ifServerLoaded {
            it.scoreboard.addPlayerToTeam(this.name.asString(), it.scoreboard.getTeam(team.name))
            teamRegistry[this.name.asString()] = team
        }
    }

    fun ServerPlayerEntity.clearTeam()
    {
        ifServerLoaded {
            it.scoreboard.clearPlayerTeam(this.name.asString())
            teamRegistry.remove(this.name.asString())
        }
    }

    fun getNonEmptyTeams(): Set<DGTeam>
    {
        return teamRegistry.values.toHashSet()
    }

    fun reset()
    {
        playerNames.clear()
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

    fun Coordinates.getInGamePlayersInRange(range: Double) = getOnlinePlayers().filter { player ->
        (inGameMap.getValue(player.name.asString())) && (Coordinates(player.x, player.y, player.z) distanceTo this <= range)
    }
}
