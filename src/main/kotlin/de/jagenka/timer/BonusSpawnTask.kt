package de.jagenka.timer

import de.jagenka.DeathGames.currentlyEnding
import de.jagenka.config.Config
import de.jagenka.managers.BonusManager
import de.jagenka.timer.BonusSpawnState.*

object BonusSpawnTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val isGameMechanic: Boolean
        get() = true
    override val runEvery: Int
        get() = 1.ticks()

    var currentState: BonusSpawnState = INACTIVE
    var time: Int = 0

    override fun run()
    {
        // if bonus platforms are disabled, don't do anything
        if (!Config.bonus.enableBonusPlatforms) return

        if (currentlyEnding) return

        if (time >= when (currentState)
            {
                INACTIVE -> return
                INITIAL -> Config.bonus.initialSpawn
                SPAWNING -> Config.bonus.spawnInterval
                DESPAWNING -> Config.bonus.stayTime
            }
        )
        {
            currentState = when (currentState)
            {
                INACTIVE -> INACTIVE

                INITIAL, SPAWNING ->
                {
                    BonusManager.activateSelectedPlatforms()
                    DESPAWNING
                }

                DESPAWNING ->
                {
                    BonusManager.disableAllPlatforms()
                    BonusManager.queueRandomPlatforms(1)
                    SPAWNING
                }
            }
            time = 0
        }

        time++
    }

    override fun reset()
    {
        BonusManager.disableAllPlatforms()

        // if bonus platforms are disabled, don't do anything
        if (!Config.bonus.enableBonusPlatforms)
        {
            currentState = INACTIVE
        } else
        {
            BonusManager.queueRandomPlatforms(1)
            time = 0
            currentState = INITIAL
        }
    }

    fun getTimeToSpawn(): Int?
    {
        return when (currentState)
        {
            INACTIVE -> null
            INITIAL -> Config.bonus.initialSpawn - time
            SPAWNING -> Config.bonus.spawnInterval - time
            DESPAWNING -> null
        }
    }

    fun getTimeToDespawn(): Int?
    {
        return when (currentState)
        {
            INACTIVE -> null
            INITIAL -> null
            SPAWNING -> null
            DESPAWNING -> Config.bonus.stayTime - time
        }
    }
}

enum class BonusSpawnState
{
    INACTIVE, INITIAL, SPAWNING, DESPAWNING
}