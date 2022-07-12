package de.jagenka.timer

import de.jagenka.DeathGames.currentlyEnding
import de.jagenka.config.Config
import de.jagenka.gameplay.rendering.GPS

object GPSTask: TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val isGameMechanic: Boolean
        get() = true
    override val runEvery: Int
        get() = 10.ticks()

    override fun run()
    {
        if (!Config.bonus.enableBonusPlatforms) return

        if (currentlyEnding) return

        GPS.drawPathToPlatformForAllPlayers()
    }

    override fun reset()
    {

    }
}