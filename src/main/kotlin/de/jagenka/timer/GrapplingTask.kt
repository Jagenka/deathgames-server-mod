package de.jagenka.timer

import de.jagenka.gameplay.graplinghook.BlackjackAndHookers

object GrapplingTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val isGameMechanic: Boolean
        get() = true
    override val runEvery: Int
        get() = 1.ticks()

    override fun run()
    {
        BlackjackAndHookers.tick()
    }

    override fun reset()
    {
        BlackjackAndHookers.reset()
    }
}