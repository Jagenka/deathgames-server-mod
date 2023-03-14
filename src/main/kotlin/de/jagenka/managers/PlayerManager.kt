package de.jagenka.managers

import de.jagenka.BlockPos
import de.jagenka.DeathGames
import de.jagenka.Util
import de.jagenka.Util.ifServerLoaded
import de.jagenka.Util.teleport
import de.jagenka.config.Config
import de.jagenka.team.DGTeam
import de.jagenka.team.ReadyCheck
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
    private val participatingMap = mutableMapOf<String, Boolean>().withDefault { false }
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

    fun getOnlineParticipatingPlayers() = getOnlinePlayers().filter { participatingMap.getValue(it.name.string) }

    fun getOnlinePlayersAround(pos: BlockPos, radius: Double) = getOnlinePlayers().filter { pos.hasInRange(it.pos, radius) }
    fun getOnlineParticipatingPlayersAround(pos: BlockPos, radius: Double) = getOnlineParticipatingPlayers().filter { pos.hasInRange(it.pos, radius) }

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
        if (teamRegistry[playerName] == team) return false
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
        Timer.schedule(1.seconds()) { makePlayerAbleToJoinAgain(playerName) }
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

    fun getParticipatingPlayers(): List<String>
    {
        return playerNames.filter { participatingMap.getValue(it) }.toList()
    }

    fun getParticipatingPlayersInTeam(team: DGTeam): List<String> = getParticipatingPlayers().filter { teamRegistry[it] == team }

    fun ServerPlayerEntity.isParticipating() = getParticipatingPlayers().contains(this.name.string)


    fun ServerPlayerEntity.makeParticipating()
    {
        participatingMap[this.name.string] = true
    }

    fun ServerPlayerEntity.eliminate()
    {
        participatingMap[this.name.string] = false
        this.changeGameMode(GameMode.SPECTATOR)
    }

    fun getParticipatingTeams() = DGTeam.values().filter { getParticipatingPlayersInTeam(it).isNotEmpty() }
    fun getOnlineParticipatingTeams() = DGTeam.values().filter { it.getOnlineParticipatingPlayers().isNotEmpty() }

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
        ReadyCheck.makeUnready(player.name.string)
    }

    fun registerAsCurrentlyDead(playerName: String) = currentlyDead.add(playerName)

    fun isCurrentlyDead(playerName: String) = playerName in currentlyDead

    @JvmStatic
    fun handleRespawn(player: ServerPlayerEntity)
    {
        if (DeathGames.running) SpawnManager.teleportPlayerToSpawn(player)
        else player.teleport(Config.lobbySpawn)
        player.addStatusEffect(StatusEffectInstance(StatusEffects.RESISTANCE, 10.seconds(), 255))

        currentlyDead.remove(player.name.string)
    }

    fun clearParticipatingStatusForEveryone()
    {
        participatingMap.clear()
    }

    fun isParticipating(playerName: String) = participatingMap.getValue(playerName)

    fun isParticipating(team: DGTeam) = getParticipatingTeams().contains(team)

    /**
     * @return is player was able to respawn (not currently alive)
     */
    fun requestRespawn(player: ServerPlayerEntity): Boolean
    {
        if (!isCurrentlyDead(player.name.string)) return false // das is doppelt zu if (player.health > 0.0f)

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
