package de.jagenka.timer

import de.jagenka.Config
import de.jagenka.DGSpawnManager

object ShuffleSpawnsTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val runEvery: Int
        get() = Config.shuffleSpawnsInterval

    private val shuffleKillDelay = Config.shuffleDelayAfterKill

    private var lastKillTime = 0

    override fun run()
    {
        Timer.schedule({ DGSpawnManager.shuffleSpawns() }, lastKillTime + shuffleKillDelay)
    }

    override fun reset()
    {
        lastKillTime = 0
    }

    fun updateLastKillTime()
    {
        lastKillTime = Timer.now()
    }
}