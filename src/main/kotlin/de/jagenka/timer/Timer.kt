package de.jagenka.timer

import de.jagenka.DeathGames
import org.spongepowered.configurate.objectmapping.ConfigSerializable

object Timer
{
    private var ticks = 0
    private var running = false

    private val tasks = mutableListOf<TimerTask>()
    private val scheduledTasks = mutableListOf<ScheduledTask>()

    init
    {
        with(tasks)
        {
            add(MoneyTask)
            add(GameOverTask)
            add(InactivePlayersTask)
            add(ShuffleSpawnsTask)
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

        scheduledTasks.forEach { if (now() >= it.time) it.task() }
    }

    fun schedule(task: () -> Unit, `in`: Int)
    {
        scheduledTasks.add(ScheduledTask(task, now() + `in`))
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


@ConfigSerializable
data class DGTime(val amount: Int, val unit: DGUnit)
{
    fun toTicks() = amount * unit.factor
}

enum class DGUnit(val factor: Int)
{
    TICKS(1), SECONDS(20), MINUTES(1200), HOURS(72000)
}

data class ScheduledTask(val task: () -> Unit, val time: Int)