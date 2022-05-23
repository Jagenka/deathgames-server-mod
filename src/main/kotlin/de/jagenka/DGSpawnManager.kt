package de.jagenka

import de.jagenka.Util.ifServerLoaded
import de.jagenka.Util.teleport
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.GameMode
import org.spongepowered.configurate.CommentedConfigurationNode

object DGSpawnManager //TODO: lobby spawn
{
    private val spawns = ArrayList<Coordinates>()
    private val teamSpawns = mutableMapOf<DGTeam?, Coordinates>().withDefault { defaultSpawn }

    private var defaultSpawn = Coordinates(0.5, 80.0, 0.5, 0f, 0f)

    private fun addSpawns(spawns: Collection<Coordinates>)
    {
        this.spawns.addAll(spawns)
    }

    private fun setSpawns(spawns: Collection<Coordinates>)
    {
        this.spawns.clear()
        addSpawns(spawns)
    }

    private fun getSpawn(team: DGTeam?) = teamSpawns.getValue(team)

    fun ServerPlayerEntity.getSpawn() = getSpawn(DGPlayerManager.getTeam(this))

    @JvmStatic
    fun handleRespawn(player: ServerPlayerEntity)
    {
        val spawn = player.getSpawn()
        player.teleport(spawn)
        player.yaw = spawn.yaw
        if (spawn == defaultSpawn) player.changeGameMode(GameMode.SPECTATOR)
    }

    fun shuffleSpawns()
    {
        shuffleSpawns(DGPlayerManager.getNonEmptyTeams())
    }

    private fun shuffleSpawns(teams: Collection<DGTeam>)
    {
        val shuffledSpawns = spawns.shuffled()
        teamSpawns.clear()
        teams.forEachIndexed { index, team ->
            if (index >= shuffledSpawns.size) return
            teamSpawns[team] = shuffledSpawns[index]
        }

        ifServerLoaded { DGTeam.BLUE.getColoredBlock().defaultState }
        //TODO: color spawn platforms
        //TODO: print message if game is running, not at the beginning
    }

    fun loadConfig(root: CommentedConfigurationNode)
    {
        setSpawns(root.node("spawns").getList(Coordinates::class.java) ?: error("Error loading DeathGames spawns from config"))
        defaultSpawn = root.node("defaultSpawn").get(Coordinates::class.java) ?: error("Error loading DeathGames defaultSpawn from config")
    }
}