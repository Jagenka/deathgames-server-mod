package de.jagenka

import de.jagenka.Util.ifServerLoaded
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
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

enum class DGTeam
{
    BLACK, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW;

    fun getColoredBlock(): Block
    {
        return when (this)
        {
            BLACK -> Blocks.BLACK_CONCRETE
            DARK_GREEN -> Blocks.GREEN_TERRACOTTA
            DARK_AQUA -> Blocks.CYAN_CONCRETE
            DARK_RED -> Blocks.RED_CONCRETE
            DARK_PURPLE -> Blocks.PURPLE_CONCRETE
            GOLD -> Blocks.ORANGE_CONCRETE
            GRAY -> Blocks.LIGHT_GRAY_CONCRETE
            DARK_GRAY -> Blocks.GRAY_CONCRETE
            BLUE -> Blocks.BLUE_CONCRETE
            GREEN -> Blocks.LIME_CONCRETE
            AQUA -> Blocks.LIGHT_BLUE_CONCRETE
            RED -> Blocks.RED_TERRACOTTA
            LIGHT_PURPLE -> Blocks.MAGENTA_CONCRETE
            YELLOW -> Blocks.YELLOW_CONCRETE
        }
    }

    fun getPlayers() = DGPlayerManager.getPlayersInTeam(this)

    fun getInGamePlayers() = DGPlayerManager.getInGamePlayersInTeam(this)

    companion object
    {
        fun random() = values().random()
    }
}

data class PlayerTeamEntry(val player: ServerPlayerEntity, val team: DGTeam)
