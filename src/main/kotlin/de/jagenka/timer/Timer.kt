package de.jagenka.timer

import de.jagenka.DeathGames

object Timer
{
    private var ticks = 0
    private var running = false

    private val tasks = mutableListOf<TimerTask>()
    private val scheduledTasks = mutableListOf<ScheduledTask>()
    private val scheduledIntervalTasks = mutableListOf<ScheduledIntervalTask>()

    init
    {
        with(tasks)
        {
            add(MoneyTask)
            add(GameOverTask)
            add(InactivePlayersTask)
            add(ShuffleSpawnsTask)
            add(BonusMoneyTask)
            add(TrapTask)
        }
    }

    @JvmStatic
    fun tick()
    {
        if (!running) return

        ticks++

        tasks.forEach {
            if (it.onlyInGame && !DeathGames.running) return@forEach
            if (ticks % it.runEvery == 0) it.run()
        }

        scheduledTasks.toList().forEach {
            if (now() >= it.time)
            {
                it.task()
                scheduledTasks.remove(it)
            }
        }

        scheduledIntervalTasks.toList().forEach {
            if ((now() - it.start) % it.interval == 0)
            {
                it.task()
            }
        }
    }

    fun schedule(task: () -> Unit, offset: Int): ScheduledTask
    {
        val scheduledTask = ScheduledTask(task, now() + offset)
        scheduledTasks.add(scheduledTask)
        return scheduledTask
    }

    fun unscheduleTask(task: ScheduledTask)
    {
        scheduledTasks.remove(task)
    }

    fun scheduleWithInterval(task: () -> Unit, offset: Int, interval: Int): ScheduledIntervalTask
    {
        val scheduledIntervalTask = ScheduledIntervalTask(task, now() + offset, interval)
        scheduledIntervalTasks.add(scheduledIntervalTask)
        return scheduledIntervalTask
    }

    fun unscheduleIntervalTask(task: ScheduledIntervalTask)
    {
        scheduledIntervalTasks.remove(task)
    }

    fun now() = ticks

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
        scheduledTasks.clear()
        scheduledIntervalTasks.clear()
    }

    fun toggle()
    {
        running = !running
    }

    fun now(unit: DGUnit) = ticks / unit.factor
}

fun Int.ticks() = this * DGUnit.TICKS.factor
fun Int.seconds() = this * DGUnit.SECONDS.factor
fun Int.minutes() = this * DGUnit.MINUTES.factor
fun Int.hours() = this * DGUnit.HOURS.factor

enum class DGUnit(val factor: Int)
{
    TICKS(1), SECONDS(20), MINUTES(1200), HOURS(72000);
}

data class ScheduledTask(val task: () -> Unit, val time: Int)
data class ScheduledIntervalTask(val task: () -> Unit, val start: Int, val interval: Int)