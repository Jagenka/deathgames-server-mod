package de.jagenka.timer

import de.jagenka.DeathGames.currentlyEnding
import de.jagenka.config.Config
import de.jagenka.managers.BonusManager

object GPSTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val isGameMechanic: Boolean
        get() = true
    override val runEvery: Int
        get() = 5.seconds()

    override fun run()
    {
        if (!Config.bonus.enableBonusPlatforms) return

        if (currentlyEnding) return

        BonusManager.updateAllCompasses()
    }

    override fun reset()
    {

    }
}