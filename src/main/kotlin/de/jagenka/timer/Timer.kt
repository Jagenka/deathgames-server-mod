package de.jagenka.timer

import de.jagenka.DeathGames

object Timer
{
    private var ticks = 0
    private var running = false

    private val tasks = mutableListOf<TimerTask>()

    init
    {
        with(tasks)
        {
            add(MoneyTask)
            add(GameOverTask)
            add(InactivePlayersTask)
        }
    }

    @JvmStatic
    fun tick()
    {
        if (!running) return

        ticks++

        tasks.forEach {
            if (it.onlyInGame && !DeathGames.running) return

            if (ticks % it.runEvery == 0) it.run()
        }
    }

    fun currentTime() = ticks

    fun isRunning() = running

    fun start()
    {
        running = true
    }

    fun pause()
    {
        running = false
    }

    fun reset()
    {
        running = false
        ticks = 0
        tasks.forEach { it.reset() }
    }

    fun toggle()
    {
        running = !running
    }

    fun currentTime(unit: DGUnit) = ticks / unit.factor
}

fun Int.ticks() = this * DGUnit.TICKS.factor
fun Int.seconds() = this * DGUnit.SECONDS.factor
fun Int.minutes() = this * DGUnit.MINUTES.factor
fun Int.hours() = this * DGUnit.HOURS.factor

enum class DGUnit(val factor: Int)
{
    TICKS(1), SECONDS(20), MINUTES(1200), HOURS(72000)
}