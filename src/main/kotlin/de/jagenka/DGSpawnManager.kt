package de.jagenka

import de.jagenka.Util.teleport
import net.minecraft.server.network.ServerPlayerEntity
import org.spongepowered.configurate.CommentedConfigurationNode

object DGSpawnManager //TODO: lobby spawn, spectator spawn, dead players spawn? -> maybe just their platform
{
    private val spawns = ArrayList<Coords>()
    private val teamSpawns = mutableMapOf<DGTeam?, Coords>().withDefault { defaultSpawn }

    private var defaultSpawn = Coords(0.5, 80.0, 0.5, 0f, 0f)

    private fun addSpawns(spawns: Collection<Coords>)
    {
        this.spawns.addAll(spawns)
    }

    private fun setSpawns(spawns: Collection<Coords>)
    {
        this.spawns.clear()
        addSpawns(spawns)
    }

    fun getSpawn(team: DGTeam?) = teamSpawns.getValue(team)

    fun getSpawn(player: ServerPlayerEntity) = getSpawn(DGPlayerManager.getTeam(player))

    @JvmStatic
    fun handleRespawn(player: ServerPlayerEntity)
    {
        val spawn = getSpawn(player)
        player.teleport(spawn)
        player.yaw = spawn.yaw
    }

    fun shuffleSpawns()
    {
        shuffleSpawns(DGPlayerManager.getNonEmptyTeams())
    }

    fun shuffleSpawns(teams: Collection<DGTeam>)
    {
        val shuffledSpawns = spawns.shuffled()
        teamSpawns.clear()
        teams.forEachIndexed { index, team -> teamSpawns[team] = shuffledSpawns[index] }
        //TODO: color spawn platforms
        //TODO: print message if game is running, not at the beginning
    }

    fun loadConfig(root: CommentedConfigurationNode)
    {
        setSpawns(root.node("spawns").getList(Coords::class.java) ?: error("Error loading DeathGames spawns from config"))
        defaultSpawn = root.node("defaultSpawn").get(Coords::class.java) ?: error("Error loading DeathGames defaultSpawn from config")
    }
}