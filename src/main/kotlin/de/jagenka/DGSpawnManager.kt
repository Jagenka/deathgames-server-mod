package de.jagenka

import net.minecraft.server.network.ServerPlayerEntity
import org.spongepowered.configurate.CommentedConfigurationNode

object DGSpawnManager
{
    private val spawns = ArrayList<Coords>()
    private val teamSpawns = HashMap<DGTeam, Coords>()

    fun addSpawns(spawns: Collection<Coords>)
    {
        this.spawns.addAll(spawns)
    }

    fun setSpawns(spawns: Collection<Coords>)
    {
        this.spawns.clear()
        addSpawns(spawns)
    }

    fun getSpawn(team: DGTeam) = teamSpawns[team]

    fun getSpawn(player: ServerPlayerEntity) = DGPlayerManager.getTeam(player)?.let { getSpawn(it) }

    fun shuffleSpawns()
    {
        shuffleSpawns(DGPlayerManager.getNonEmptyTeams())
    }

    fun shuffleSpawns(teams: Collection<DGTeam>)
    {
        val shuffledSpawns = spawns.shuffled()
        teamSpawns.clear()
        teams.forEachIndexed { index, team -> teamSpawns[team] = shuffledSpawns[index] }
    }

    fun loadConfig(root: CommentedConfigurationNode)
    {
        setSpawns(root.node("spawns").getList(Coords::class.java) ?: error("Error loading DeathGames spawns from config"))
    }
}