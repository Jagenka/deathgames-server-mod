package de.jagenka.timer

import de.jagenka.DeathGames.currentlyEnding
import de.jagenka.gameplay.rendering.GPS

object GPSTask: TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val isGameMechanic: Boolean
        get() = true
    override val runEvery: Int
        get() = 2

    override fun run()
    {
        if (currentlyEnding) return

        GPS.makeArrowGoBrrr()
    }

    override fun reset()
    {

    }
}