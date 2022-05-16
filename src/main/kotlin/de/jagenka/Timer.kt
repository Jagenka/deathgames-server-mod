package de.jagenka

object Timer
{
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

    fun stop()
    {
        running = false
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