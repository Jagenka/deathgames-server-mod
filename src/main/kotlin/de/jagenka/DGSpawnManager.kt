package de.jagenka

object DGSpawnManager
{
    private val spawns = ArrayList<Coords>()

    fun addSpawns(spawns: Collection<Coords>)
    {
        this.spawns.addAll(spawns)
    }

    fun setSpawns(spawns: Collection<Coords>)
    {
        this.spawns.clear()
        addSpawns(spawns)
    }
}