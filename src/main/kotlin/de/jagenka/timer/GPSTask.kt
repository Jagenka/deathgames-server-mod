package de.jagenka.timer

import de.jagenka.GPS

object GPSTask: TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val runEvery: Int
        get() = 1.seconds()

    override fun run()
    {
        GPS.makeArrowGoBrrr()
    }

    override fun reset()
    {

    }
}