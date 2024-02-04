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
        get() = 2

    override fun run()
    {
        if (!Config.configEntry.bonus.enableBonusPlatforms) return

        if (currentlyEnding) return

        GPS.showArrowToNextBonusPlatform()
    }

    override fun reset()
    {

    }
}