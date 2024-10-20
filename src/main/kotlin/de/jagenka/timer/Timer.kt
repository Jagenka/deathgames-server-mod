package de.jagenka.timer

import de.jagenka.DeathGames
import kotlinx.datetime.Clock

object Timer
{
    private var ticks = 0
    private var running = true

    private val tasks = mutableListOf<TimerTask>()
    private val scheduledTasks = mutableListOf<ScheduledTask>()
    private val intervalTasks = mutableSetOf<IntervalTask>()

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
            add(GrapplingTask)
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

        intervalTasks.toList().forEach {
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
        val scheduledTask = ScheduledTask(time, task)
        scheduledTasks.add(scheduledTask)
        return scheduledTask
    }

    fun unscheduleTask(task: ScheduledTask)
    {
        scheduledTasks.remove(task)
    }

    fun scheduleWithInterval(name: String, interval: Int, task: () -> Unit): IntervalTask?
    {
        intervalTasks.find { it.name == name }?.let { // if task already exists
            return it
        }
        val newIntervalTask = IntervalTask(name, interval, task)
        intervalTasks.add(newIntervalTask)
        return newIntervalTask
    }

    fun removeIntervalTask(name: String)
    {
        intervalTasks.removeIf { it.name == name }
    }

    fun newCustomTimer(name: String = Clock.System.now().toString()): CustomTimer
    {
        val customTimer = CustomTimer(name)
        return customTimers.find { it.name == name } // return timer already in storage instead of adding a new one
            ?: customTimers.add(customTimer).let { customTimer } // add it, and return customTimer (let necessary because add returns true)
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
        intervalTasks.clear()
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

data class ScheduledTask(val time: Int, val task: () -> Unit)
data class IntervalTask(val name: String, val interval: Int, val task: () -> Unit)
{
    val start: Int = Timer.now()
}

data class CustomTimer(val name: String)
{
    var time: Int = 0
}