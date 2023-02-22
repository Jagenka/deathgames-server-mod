package de.jagenka.gameplay.graplinghook

import de.jagenka.plus
import de.jagenka.times
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.entity.projectile.PersistentProjectileEntity
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.pow

object BlackjackAndHookers
{
    const val GRAVITY_ACCELERATION = 0.05 // In m/tick² - Calculated from 1 m/s² (Source: Minecraft Wiki => Arrow)

    private val activeHooks = mutableListOf<ArrowHook>()

    @JvmStatic
    fun tick()
    {
        activeHooks.toList().forEach {
            if (it.isAlive())
            {
                it.age++
            }
            else
            {
                it.killEntity()
                activeHooks.remove(it)
            }
        }
    }

    @JvmStatic
    fun forceTheHooker(world: World, owner: PlayerEntity): Boolean
    {
        owner.fishHook?.let { bobber ->
            if (!bobber.isOnGround) return false

            val yVector = Vec3d(0.0, bobber.pos.y - owner.pos.y, 0.0)
            val xzVector = Vec3d(bobber.pos.x - owner.pos.x, 0.0, bobber.pos.z - owner.pos.z)

            val (yVelocity, flightTime) = getVerticalVelocity(yVector.length())
            val xzVelocity = getHorizontalVelocity(xzVector.length(), flightTime)

            val totalVelocity = xzVector.normalize() * xzVelocity + yVector.normalize() * yVelocity

            val arrow = ArrowEntity(world, owner)
            arrow.velocity = totalVelocity
            arrow.damage = 1.0
            arrow.pickupType = PersistentProjectileEntity.PickupPermission.DISALLOWED
            arrow.pierceLevel = 16
            arrow.isSilent = true
            arrow.customName = Text.of("hook")
            arrow.isCustomNameVisible = false
            world.spawnEntity(arrow)
            activeHooks.add(ArrowHook(arrow, flightTime))
            owner.startRiding(arrow)
        } ?: return false
        return true
    }

    /**
     * Determines the velocity needed to cover given distance in given time. (In m/tick)
     * @return The calculated velocity
     * @param distance The distance to be covered in blocks
     * @param flyTime The planned flight time in ticks
     */
    private fun getHorizontalVelocity(distance: Double, flyTime: Int): Double
    {
        // d(N) = v(N) * N - (1-0.99^N) * v(N) => Taylor expansion required => Try simulated approach
        // v(N) = v(N - 1) * 0.99 with N: number of ticks elapsed => v = v_0 * 0.99^N
        // d = v * N => d(N) = v_0 * 0.99^N * N
        // d(N) = v_0 * 0.99^N * N
        if (flyTime > 100) return 0.0 // Don't calculate for too much flight time

        var sum = 0.0

        for (N in 1..flyTime)
        {
            sum += 0.99.pow(N)
        }

        return distance/sum
    }

    /**
     * Simulated approach to calculate the required velocity (m/tick) and flight time (ticks).
     * @return A pair of the calculated velocity (first) and flight time (second)
     * @param distance The distance to be traveled.
     */
    private fun getVerticalVelocity(distance: Double): Pair<Double, Int>
    {
        // d(N) = v(N) * N + (1/2) * g(N) * N²
        // v(N) = v(N - 1) * 0.99 - g(N) * N
        val timeout = 100

        var tickCount = 0
        var velocity = 0.0
        var coveredDistance = 0.0

        while (coveredDistance < distance && tickCount < timeout)
        {
            tickCount++
            coveredDistance += velocity
            velocity /= 0.99
            velocity += GRAVITY_ACCELERATION
        }

        return Pair(velocity, tickCount)
    }

    data class ArrowHook(private val arrow: ArrowEntity, private val maxAge: Int, var age: Int = 0)
    {
        fun isAlive(): Boolean = age < maxAge
        fun killEntity(): Unit = arrow.kill()
    }
}
