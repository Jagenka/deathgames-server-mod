package de.jagenka.timer

import de.jagenka.DeathGames

object Timer
{
    private var ticks = 0
    private var running = true

    private val tasks = mutableListOf<TimerTask>()
    private val scheduledTasks = mutableListOf<ScheduledTask>()
    private val scheduledIntervalTasks = mutableListOf<ScheduledIntervalTask>()

    private val customTimers = mutableListOf<CustomTimer>()

    var gameMechsPaused: Boolean = false

    init
    {
        with(tasks)
        {
            add(BasicTpTask)
            add(MoneyTask)
            add(GameOverTask)
            add(InactivePlayersTask)
            add(ShuffleSpawnsTask)
            add(BonusMoneyTask)
            add(TrapTask)
            add(BonusDisplayTask)
            add(ShopTask)
            add(KeepInBoundsTask)
            add(LobbyTask)
            add(GPSTask)
            add(CaptureSpawnTask)
            add(BonusSpawnTask)
        }
    }

    @JvmStatic
    fun tick()
    {
        if (!this.running) return

        if (!DeathGames.running) DeathGames.currentlyEnding = false

        ticks++

        tasks.forEach {
            if (gameMechsPaused && it.isGameMechanic) return@forEach
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

        customTimers.toList().forEach {
            it.time++
        }
    }

    fun schedule(offset: Int, task: () -> Unit): ScheduledTask
    {
        return scheduleAt(task, now() + offset)
    }

    fun scheduleAt(task: () -> Unit, time: Int): ScheduledTask
    {
        val scheduledTask = ScheduledTask(task, time)
        scheduledTasks.add(scheduledTask)
        return scheduledTask
    }

    fun unscheduleTask(task: ScheduledTask)
    {
        scheduledTasks.remove(task)
    }

    fun scheduleWithInterval(task: () -> Unit, offset: Int, interval: Int): ScheduledIntervalTask
    {
        val scheduledIntervalTask = ScheduledIntervalTask.getFor(task, offset, interval)
        scheduleWithInterval(scheduledIntervalTask)
        return scheduledIntervalTask
    }

    fun scheduleWithInterval(task: ScheduledIntervalTask)
    {
        if (!scheduledIntervalTasks.contains(task)) scheduledIntervalTasks.add(task)
    }

    fun unscheduleIntervalTask(task: ScheduledIntervalTask)
    {
        scheduledIntervalTasks.remove(task)
    }

    fun newCustomTimer(): CustomTimer = newCustomTimer()

    fun newCustomTimer(name: String): CustomTimer
    {
        val customTimer = CustomTimer(name)
        return customTimers.find { it.name == name } ?: customTimers.add(customTimer).let { customTimer }
    }

    fun removeCustomTimer(customTimer: CustomTimer)
    {
        customTimers.remove(customTimer)
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
        gameMechsPaused = true
        tasks.forEach { it.reset() }
        scheduledTasks.clear()
        scheduledIntervalTasks.clear()
        customTimers.clear()
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
{
    companion object
    {
        fun getFor(task: () -> Unit, offset: Int, interval: Int) = ScheduledIntervalTask(task, Timer.now() + offset, interval)
    }
}

data class CustomTimer(val name: String = System.currentTimeMillis().toString())
{
    var time: Int = 0
}