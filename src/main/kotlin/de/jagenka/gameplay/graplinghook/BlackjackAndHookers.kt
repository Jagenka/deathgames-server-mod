package de.jagenka.gameplay.graplinghook

import de.jagenka.plus
import de.jagenka.shop.Shop
import de.jagenka.times
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.entity.projectile.PersistentProjectileEntity
import net.minecraft.text.Text
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.pow

object BlackjackAndHookers
{
    const val GRAVITY_ACCELERATION = 0.05 // In m/tick² - Calculated from 1 m/s² (Source: Minecraft Wiki => Arrow)
    const val MAX_DISTANCE = 15.0 // In m
    const val MAX_COOLDOWN = 100 // In ticks

    val activeHooks = mutableListOf<ArrowHook>()
    private val cooldown = Cooldown(MAX_COOLDOWN)

    @JvmStatic
    fun tick()
    {
        cooldown.tickDown()
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
        if (Shop.isInShopBounds(owner) || !cooldown.isReady()) return false

        val hitResult = owner.raycast(MAX_DISTANCE, 0f, false)
        if (hitResult.type != HitResult.Type.BLOCK) return false

        val targetPos = hitResult.pos + Vec3d(0.0, 0.5, 0.0)
        if (owner.pos.y > targetPos.y + 1) return false

        val yVector = Vec3d(0.0, targetPos.y - owner.pos.y, 0.0)
        val xzVector = Vec3d(targetPos.x - owner.pos.x, 0.0, targetPos.z - owner.pos.z)

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

        cooldown.goOnCooldown()

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

    /**
     * Class for tracking the arrow, which the player is riding on.
     */
    data class ArrowHook(private val arrow: ArrowEntity, private val maxAge: Int, var age: Int = 0)
    {
        fun isAlive(): Boolean = age < maxAge
        fun killEntity(): Unit = arrow.kill()
    }

    /**
     * Implements a functional cooldown.
     */
    data class Cooldown(private val maxCooldown: Int, private var remainingCooldown: Int = 0)
    {
        fun isReady(): Boolean = remainingCooldown <= 0
        fun getCooldown(): Double = remainingCooldown.toDouble() / maxCooldown
        fun goOnCooldown(): Unit
        {
            remainingCooldown = maxCooldown
        }
        fun tickDown(): Unit
        {
            if (remainingCooldown > 0) remainingCooldown--
        }
    }
}
