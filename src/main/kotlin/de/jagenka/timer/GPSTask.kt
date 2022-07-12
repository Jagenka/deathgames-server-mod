package de.jagenka.timer

import de.jagenka.DeathGames.currentlyEnding
import de.jagenka.gameplay.rendering.GPS

object GPSTask: TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val runEvery: Int
        get() = 10.ticks()

    override fun run()
    {
        if (currentlyEnding) return

        GPS.drawPathToPlatformForAllPlayers()
    }

    override fun reset()
    {

    }
}