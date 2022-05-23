package de.jagenka

import de.jagenka.Util.ifServerLoaded
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Formatting
import net.minecraft.world.GameMode

object DGPlayerManager
{
    private val players = mutableSetOf<DGPlayer>()
    private val teamRegistry = mutableMapOf<ServerPlayerEntity, DGTeam>()

    fun getPlayer(name: String): ServerPlayerEntity?
    {
        getPlayers().forEach { if (it.name.asString() == name) return it }
        return null
    }

    fun getPlayers(): Set<ServerPlayerEntity>
    {
        val allPlayers = mutableSetOf<ServerPlayerEntity>()
        players.forEach { allPlayers.add(it.playerEntity) }
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

    fun getInGamePlayers(): List<ServerPlayerEntity>
    {
        val result = mutableListOf<ServerPlayerEntity>()
        players.filter { it.inGame }.forEach { result.add(it.playerEntity) }
        return result.toList()
    }

    fun getInGamePlayersInTeam(team: DGTeam): List<ServerPlayerEntity> = getInGamePlayers().filter { teamRegistry[it] == team }

    fun ServerPlayerEntity.isInGame() = getInGamePlayers().contains(this)


    fun ServerPlayerEntity.makeInGame()
    {
        players.add(DGPlayer(this, true))
    }

    fun ServerPlayerEntity.eliminate()
    {
        players.removeIf { it.playerEntity == this }
        players.add(DGPlayer(this, false))
        this.changeGameMode(GameMode.SPECTATOR)
    }

    fun getInGameTeams() = DGTeam.values().filter { it.getInGamePlayers().isNotEmpty() }

    fun Coords.getInGamePlayersInRange(range: Double) = getInGamePlayers().filter { player ->
        Coords(player.x, player.y, player.z) distanceTo this <= range
    }

    @JvmStatic
    fun replaceDeadPlayer(old: ServerPlayerEntity, new: ServerPlayerEntity)
    {
        val oldEntry = players.find { it.playerEntity == old }
        if (oldEntry != null)
        {
            players.remove(oldEntry)
            players.add(DGPlayer(new, oldEntry.inGame))
        }

        val oldTeam = teamRegistry.remove(old)
        if (oldTeam != null) teamRegistry[new] = oldTeam
    }
}

data class DGPlayer(var playerEntity: ServerPlayerEntity, var inGame: Boolean)
{
    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DGPlayer

        if (playerEntity != other.playerEntity) return false

        return true
    }

    override fun hashCode(): Int
    {
        return playerEntity.hashCode()
    }
}

data class PlayerTeamEntry(val player: ServerPlayerEntity, val team: DGTeam)
