package de.jagenka.timer

import de.jagenka.DeathGames.currentlyEnding
import de.jagenka.gameplay.rendering.GPS

object GPSTask: TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val runEvery: Int
        get() = 1.seconds()

    override fun run()
    {
        if (currentlyEnding) return

        if (Timer.gameMechsPaused) return

        GPS.makeArrowGoBrrr()
    }

    override fun reset()
    {

    }
}