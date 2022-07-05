package de.jagenka.timer

import de.jagenka.gameplay.traps.TrapsAreNotGay

object TrapTask: TimerTask
{
    override val onlyInGame: Boolean
        get() = false
    // TODO: make tru
    override val runEvery: Int
        get() = 1.ticks()

    override fun run()
    {
        TrapsAreNotGay.tick()
    }

    override fun reset()
    {
        TrapsAreNotGay.becomeGay()
    }
}