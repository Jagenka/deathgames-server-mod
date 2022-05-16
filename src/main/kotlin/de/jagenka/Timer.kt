package de.jagenka

import net.minecraft.server.network.ServerPlayerEntity

object Timer
{
    private val inactiveTimer = mutableMapOf<ServerPlayerEntity, Int>().withDefault { 0 } //TODO: highlight inactive players

    private var ticks = 0
    private var running = false

    @JvmStatic
    fun tick()
    {
        if (running)
        {
            ticks++

            onFullTick()
            if (ticks % DGUnit.SECONDS.factor == 0) onFullSecond()
            if (ticks % DGUnit.MINUTES.factor == 0) onFullMinute()
            if (ticks % DGUnit.HOURS.factor == 0) onFullHour()
        }
    }

    private fun onFullTick()
    {
        DGPlayerManager.getInGamePlayers().forEach { inactiveTimer[it] = inactiveTimer.getValue(it) + 1 }

        if (DGPlayerManager.getInGameTeams().size <= 1) TODO("game end not implemented")
    }

    private fun onFullSecond()
    {
//        println("${currentTime(DGUnit.SECONDS)}s")
    }

    private fun onFullMinute()
    {
//        println("${currentTime(DGUnit.MINUTES)}min")
    }

    private fun onFullHour()
    {

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