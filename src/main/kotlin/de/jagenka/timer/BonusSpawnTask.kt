package de.jagenka.timer

import de.jagenka.DeathGames
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
        if (DeathGames.currentlyEnding) return

        if (time >= when (currentState)
            {
                INACTIVE -> return
                INITIAL -> Config.bonusPlatformInitialSpawn
                SPAWNING -> Config.bonusPlatformSpawnInterval
                DESPAWNING -> Config.bonusPlatformStayTime
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
        BonusManager.queueRandomPlatforms(1)
        time = 0
        currentState = INITIAL
    }

    fun getTimeToSpawn(): Int?
    {
        return when (currentState)
        {
            INACTIVE -> null
            INITIAL -> Config.bonusPlatformInitialSpawn - time
            SPAWNING -> Config.bonusPlatformSpawnInterval - time
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
            DESPAWNING -> Config.bonusPlatformStayTime - time
        }
    }
}

enum class BonusSpawnState
{
    INACTIVE, INITIAL, SPAWNING, DESPAWNING
}