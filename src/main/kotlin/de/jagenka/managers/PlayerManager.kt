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
import net.minecraft.ChatFormatting
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.GameType

object PlayerManager
{
    private val playerNames = mutableSetOf<String>()
    private val participatingMap = mutableMapOf<String, Boolean>().withDefault { false }
    private val teamRegistry = mutableMapOf<String, DGTeam>()

    private val currentlyDead = mutableSetOf<String>()
    private val recentlyRespawned = mutableSetOf<String>()

    private val canPlayerJoin = mutableMapOf<String, Boolean>().withDefault { true }

    fun getOnlinePlayer(name: String): ServerPlayer? = getOnlinePlayers().find { it.name.string == name }

    fun getOnlinePlayers(): Set<ServerPlayer>
    {
        val allPlayers = mutableSetOf<ServerPlayer>()
        ifServerLoaded { allPlayers.addAll(it.playerList.players.toList()) }
        return allPlayers.toSet()
    }

    fun getOnlineParticipatingPlayers() = getOnlinePlayers().filter { participatingMap.getValue(it.name.string) }

    fun getOnlinePlayersAround(pos: BlockPos, radius: Double) =
        getOnlinePlayers().filter { pos.hasInRange(it.position(), radius) }

    fun getOnlineParticipatingPlayersAround(pos: BlockPos, radius: Double) =
        getOnlineParticipatingPlayers().filter { pos.hasInRange(it.position(), radius) }

    fun getPlayers(): Set<String>
    {
        ifServerLoaded { server ->
            server.playerList.players.forEach {
                if (!playerNames.contains(it.name.string)) playerNames.add(it.name.string)
            }
        }

        return playerNames.toSet()
    }

    fun getTeam(player: ServerPlayer) = getTeam(player.name.string)
    fun getTeam(playerName: String) = teamRegistry[playerName]

    fun ServerPlayer.getDGTeam() = getTeam(this)

    fun ServerPlayer.addToDGTeam(team: DGTeam) = addPlayerToTeam(this, team)

    /**
     * @return if player was added to team
     */
    fun addPlayerToTeam(player: ServerPlayer, team: DGTeam): Boolean
    {
        if (DeathGames.running) return false
        val playerName = player.name.string
        if (teamRegistry[playerName] == team) return false
        if (canPlayerJoin.getValue(playerName))
        {
            ifServerLoaded {
                it.scoreboard.addPlayerToTeam(playerName, it.scoreboard.getPlayerTeam(team.name)!!)
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

    fun ServerPlayer.kickFromDGTeam() = kickPlayerFromTeam(this)

    /**
     * @return if player left their team
     */
    fun kickPlayerFromTeam(player: ServerPlayer): Boolean
    {
        val playerName = player.name.string
        if (canPlayerJoin.getValue(playerName))
        {
            ifServerLoaded {
                it.scoreboard.removePlayerFromTeam(playerName)
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
            DGTeam.entries.forEach { color ->
                server.scoreboard.addPlayerTeam(color.name)
                server.scoreboard.getPlayerTeam(color.name)?.color =
                    ChatFormatting.getByName(color.name.lowercase()) ?: ChatFormatting.WHITE
            }
        }
    }

    fun getTeamPlayers() = getOnlinePlayers().filter { teamRegistry[it.name.string] != null }

    fun getPlayersInTeam(team: DGTeam): List<String>
    {
        return teamRegistry.keys.filter { teamRegistry[it] == team }
    }

    fun getOnlinePlayersInTeam(team: DGTeam): List<ServerPlayer>
    {
        return getOnlinePlayers().filter { it.getDGTeam() == team }
    }

    fun getParticipatingPlayers(): List<String>
    {
        return playerNames.filter { participatingMap.getValue(it) }.toList()
    }

    fun getParticipatingPlayersInTeam(team: DGTeam): List<String> = getParticipatingPlayers().filter { teamRegistry[it] == team }

    fun ServerPlayer.isParticipating() = getParticipatingPlayers().contains(this.name.string)


    fun addParticipant(playerName: String)
    {
        participatingMap[playerName] = true
    }

    fun removeParticipant(playerName: String) = eliminate(playerName)

    fun eliminate(playerName: String)
    {
        participatingMap[playerName] = false
        getOnlinePlayer(playerName)?.setGameMode(GameType.SPECTATOR)
    }

    fun getParticipatingTeams() = DGTeam.entries.filter { getParticipatingPlayersInTeam(it).isNotEmpty() }
    fun getOnlineParticipatingTeams() = DGTeam.entries.filter { it.getOnlineParticipatingPlayers().isNotEmpty() }

    @JvmStatic
    fun onPlayerJoin(player: ServerPlayer)
    {
        Util.minecraftServer?.let { server -> player.resetRecipes(server.recipeManager.recipes) }

        if (player.getDGTeam() == null)
        {
            ifServerLoaded { server ->
                server.scoreboard.removePlayerFromTeam(player.name.string)
            }
            if (DeathGames.running)
            {
                SpawnManager.spawnPlayer(player)
                player.setGameMode(GameType.SPECTATOR)
            }
        }

        DisplayManager.updateLevelDisplay()
        DisplayManager.resetBossBars()

        if (!DeathGames.running && Util.minecraftServer?.playerList?.isOp(player.nameAndId()) != true) //is not op
        {
            player.inventory.clearContent()
            player.setGameMode(GameType.ADVENTURE)
        }
    }

    @JvmStatic
    fun onPlayerLeave(player: ServerPlayer)
    {
        if (!DeathGames.running) player.kickFromDGTeam()
        ReadyCheck.makeUnready(player.name.string)
    }

    fun registerAsCurrentlyDead(playerName: String) = currentlyDead.add(playerName)

    fun isCurrentlyDead(playerName: String) = playerName in currentlyDead

    fun hasRecentlyRespawned(playerName: String) = playerName in recentlyRespawned

    @JvmStatic
    fun handleRespawn(player: ServerPlayer)
    {
        if (DeathGames.running)
        {
            SpawnManager.spawnPlayer(player, giveItems = true)

            val playerName = player.name.string
            currentlyDead.remove(playerName)
            recentlyRespawned.add(playerName)
            Timer.schedule(1.seconds()) { recentlyRespawned.remove(playerName) }
        } else
        {
            player.teleport(Config.spawns.lobbySpawn)
        }
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
    // TODO: test this
    fun requestRespawn(player: ServerPlayer): Boolean
    {
        if (!isCurrentlyDead(player.name.string)) return false // das is doppelt zu if (player.health > 0.0f)

        Util.minecraftServer?.let { server ->
            if (player.health > 0.0f || player.isAlive)
            {
                return false
            }

            val newPlayer = server.playerList.respawn(player, true, Entity.RemovalReason.DISCARDED)

//            player.networkHandler.player = server.playerManager.respawnPlayer(player, true, Entity.RemovalReason.DISCARDED)
//                Criteria.CHANGED_DIMENSION.trigger(player, World.END, World.OVERWORLD)
//                return true


            //player.networkHandler.player = server.playerManager.respawnPlayer(player, false, Entity.RemovalReason.DISCARDED)
            return true
        } ?: return false
    }

    fun resetForGameStart()
    {
        currentlyDead.clear()
        recentlyRespawned.clear()
    }

    fun ServerPlayer.isOp(): Boolean = this.level().server.playerList.isOp(this.nameAndId())
}
