package de.jagenka.timer

import de.jagenka.DeathGames.currentlyEnding
import de.jagenka.config.Config
import de.jagenka.config.Config.shuffleDelayAfterKill
import de.jagenka.config.Config.shuffleSpawnsInterval
import de.jagenka.managers.SpawnManager

object ShuffleSpawnsTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val isGameMechanic: Boolean
        get() = true
    override val runEvery: Int
        get() = shuffleSpawnsInterval

    private val shuffleKillDelay = shuffleDelayAfterKill

    private var lastKillTime = 0

    override fun run()
    {
        if (!Config.configEntry.spawns.enableShuffle) return

        if (currentlyEnding) return

        Timer.scheduleAt({ SpawnManager.shuffleSpawns() }, lastKillTime + shuffleKillDelay)
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