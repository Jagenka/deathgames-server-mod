package de.jagenka.timer

interface TimerTask
{
    val onlyInGame: Boolean
    val runEvery: Int // number of ticks in between runs

    fun run()

    fun reset()
}