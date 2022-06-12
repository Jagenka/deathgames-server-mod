package de.jagenka.managers

import de.jagenka.Coordinates
import de.jagenka.DeathGames
import de.jagenka.Util
import de.jagenka.Util.ifServerLoaded
import de.jagenka.Util.teleport
import de.jagenka.config.Config
import de.jagenka.team.DGTeam
import de.jagenka.timer.Timer
import de.jagenka.timer.seconds
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Formatting
import net.minecraft.world.GameMode
import net.minecraft.world.World

object PlayerManager
{
    private val playerNames = mutableSetOf<String>()
    private val inGameMap = mutableMapOf<String, Boolean>().withDefault { false } // TODO participating
    private val teamRegistry = mutableMapOf<String, DGTeam>()

    private val currentlyDead = mutableSetOf<String>()

    private val canPlayerJoin = mutableMapOf<String, Boolean>().withDefault { true }

    fun getOnlinePlayer(name: String): ServerPlayerEntity? = getOnlinePlayers().find { it.name.string == name }

    fun getOnlinePlayers(): Set<ServerPlayerEntity>
    {
        val allPlayers = mutableSetOf<ServerPlayerEntity>()
        ifServerLoaded { allPlayers.addAll(it.playerManager.playerList) }
        return allPlayers.toSet()
    }

    fun getOnlineInGamePlayers() = getOnlinePlayers().filter { inGameMap.getValue(it.name.string) }

    fun getPlayers(): Set<String>
    {
        ifServerLoaded { server ->
            server.playerManager.playerList.forEach {
                if (!playerNames.contains(it.name.string)) playerNames.add(it.name.string)
            }
        }

        return playerNames.toSet()
    }

    fun getTeam(player: ServerPlayerEntity) = getTeam(player.name.string)
    fun getTeam(playerName: String) = teamRegistry[playerName]

    fun ServerPlayerEntity.getDGTeam() = getTeam(this)

    fun ServerPlayerEntity.addToDGTeam(team: DGTeam) = addPlayerToTeam(this, team)

    /**
     * @return if player was added to team
     */
    fun addPlayerToTeam(player: ServerPlayerEntity, team: DGTeam): Boolean
    {
        if (DeathGames.running) return false
        val playerName = player.name.string
        if (canPlayerJoin.getValue(playerName))
        {
            ifServerLoaded {
                it.scoreboard.addPlayerToTeam(playerName, it.scoreboard.getTeam(team.name))
                teamRegistry[playerName] = team
            }
            disableTeamJoinForSomeTime(playerName)
            return true
        }
        return false
    }

    fun disableTeamJoinForSomeTime(playerName: String)
    {
        canPlayerJoin[playerName] = false
        Timer.schedule({ makePlayerAbleToJoinAgain(playerName) }, 1.seconds())
    }

    fun makePlayerAbleToJoinAgain(playerName: String)
    {
        canPlayerJoin[playerName] = true
    }

    fun ServerPlayerEntity.kickFromDGTeam() = kickPlayerFromTeam(this)

    /**
     * @return if player left their team
     */
    fun kickPlayerFromTeam(player: ServerPlayerEntity): Boolean
    {
        val playerName = player.name.string
        if (canPlayerJoin.getValue(playerName))
        {
            ifServerLoaded {
                it.scoreboard.clearPlayerTeam(playerName)
                teamRegistry.remove(playerName)
            }
            disableTeamJoinForSomeTime(playerName)
            return true
        }
        return false
    }

    fun getNonEmptyTeams(): Set<DGTeam>
    {
        return teamRegistry.values.toHashSet()
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

    fun getTeamPlayers() = getOnlinePlayers().filter { teamRegistry[it.name.string] != null }

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

    fun ServerPlayerEntity.isInGame() = getInGamePlayers().contains(this.name.string)


    fun ServerPlayerEntity.makeInGame()
    {
        inGameMap[this.name.string] = true
    }

    fun ServerPlayerEntity.eliminate()
    {
        inGameMap[this.name.string] = false
        this.changeGameMode(GameMode.SPECTATOR)
    }

    fun getInGameTeams() = DGTeam.values().filter { getInGamePlayersInTeam(it).isNotEmpty() }
    fun getOnlineInGameTeams() = DGTeam.values().filter { it.getOnlineInGamePlayers().isNotEmpty() }

    fun Coordinates.getInGamePlayersInRange(range: Double) = getOnlinePlayersInRange(range).filter { player ->
        inGameMap.getValue(player.name.string)
    }

    fun Coordinates.getOnlinePlayersInRange(range: Double) = getOnlinePlayers().filter { player ->
        this distanceTo player.pos <= range
    }

    @JvmStatic
    fun onPlayerJoin(player: ServerPlayerEntity)
    {
        if (player.getDGTeam() == null)
        {
            ifServerLoaded { server ->
                server.scoreboard.clearPlayerTeam(player.name.string)
            }
            if (DeathGames.running)
            {
                SpawnManager.teleportPlayerToSpawn(player)
                player.changeGameMode(GameMode.SPECTATOR)
            }
        }

        DisplayManager.updateLevelDisplay()
        DisplayManager.resetBossBars()

        if (!DeathGames.running && !player.hasPermissionLevel(2)) //is not op
        {
            player.inventory.clear()
            player.changeGameMode(GameMode.ADVENTURE)
        }
    }

    @JvmStatic
    fun onPlayerLeave(player: ServerPlayerEntity)
    {
        if (!DeathGames.running) player.kickFromDGTeam()
    }

    fun registerAsCurrentlyDead(playerName: String) = currentlyDead.add(playerName)

    fun isCurrentlyDead(playerName: String) = playerName in currentlyDead

    @JvmStatic
    fun handleRespawn(player: ServerPlayerEntity)
    {
        if (DeathGames.running) SpawnManager.teleportPlayerToSpawn(player)
        else player.teleport(Config.lobbySpawn)
        player.addStatusEffect(StatusEffectInstance(StatusEffects.RESISTANCE, 5.seconds(), 255))

        currentlyDead.remove(player.name.string)
    }

    fun clearInGameStatusForEveryone()
    {
        inGameMap.clear()
    }

    fun isInGame(playerName: String) = inGameMap.getValue(playerName)

    fun requestRespawn(player: ServerPlayerEntity): Boolean
    {
        if (!isCurrentlyDead(player.name.string)) return false

        Util.minecraftServer?.let { server ->
            if (player.notInAnyWorld)
            {
                player.notInAnyWorld = false
                player.networkHandler.player = server.playerManager.respawnPlayer(player, true)
                Criteria.CHANGED_DIMENSION.trigger(player, World.END, World.OVERWORLD)
                return true
            }
            if (player.health > 0.0f)
            {
                return false
            }
            player.networkHandler.player = server.playerManager.respawnPlayer(player, false)
            return true
        } ?: return false
    }
}
