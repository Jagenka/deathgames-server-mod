package de.jagenka.timer

import de.jagenka.gameplay.graplinghook.GrapplingHook

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
        GrapplingHook.usableHooks.forEach { it.tick() }
    }

    override fun reset()
    {
        GrapplingHook.usableHooks.forEach { it.reset() }
    }
}