package de.jagenka.timer

interface TimerTask
{
    /**
     * if true, this task will only run, when the game has started.
     * if `isGameMechanic` is false, this task will run starting with start in shop.
     */
    val onlyInGame: Boolean

    /**
     * if true, this task will be paused during start in shop.
     */
    val isGameMechanic: Boolean

    /**
     * number of ticks in-between runs.
     */
    val runEvery: Int

    /**
     * this will be called by the timer every runEvery ticks.
     */
    fun run()

    /**
     * this will be called by the timer, when it is reset via Timer::reset.
     */
    fun reset()
}