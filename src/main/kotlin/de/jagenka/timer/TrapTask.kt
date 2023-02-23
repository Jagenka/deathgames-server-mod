package de.jagenka.timer

import de.jagenka.gameplay.traps.TrapsAreNotGay

object TrapTask: TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val isGameMechanic: Boolean
        get() = true

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