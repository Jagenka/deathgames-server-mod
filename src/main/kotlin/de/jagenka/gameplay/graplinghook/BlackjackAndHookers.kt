package de.jagenka.gameplay.graplinghook

import de.jagenka.DeathGames
import de.jagenka.managers.PlayerManager
import de.jagenka.plus
import de.jagenka.shop.Shop
import net.minecraft.component.DataComponentTypes.CUSTOM_DATA
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.sqrt

object BlackjackAndHookers
{
    const val GRAVITY_ACCELERATION = 0.08 // In m/tick² - (Source: Minecraft Wiki => https://minecraft.fandom.com/wiki/Entity)

    val activeHooks = mutableListOf<ArrowHook>()
    val cooldowns = mutableMapOf<ItemStack, Cooldown>()

    @JvmStatic
    val itemItem: Item = Items.CARROT_ON_A_STICK

    fun tick()
    {
        PlayerManager.getOnlinePlayers().forEach { player ->
            if (!isRidingHook(player))
            {
                player.setNoGravity(false)
            }
        }
        cooldowns.values.forEach {
            it.tickDown()
        }
        activeHooks.toList().forEach {
            if (!it.vehicle.hasPlayerRider())
            {
                it.killEntity()
                activeHooks.remove(it)
            }
            if (!it.isAlive())
            {
                val pos = it.getEndPosition()
                val owner = it.owner
                it.killEntity()
                owner.teleport(pos.x, pos.y, pos.z, false)
                owner.setNoGravity(false)
                activeHooks.remove(it)
            } else it.tick()
        }
    }

    fun reset()
    {
        activeHooks.toList().forEach {
            it.killEntity()
            activeHooks.remove(it)
        }

        cooldowns.clear()
    }

    @JvmStatic
    fun forceTheHooker(world: World, owner: ServerPlayerEntity, itemStackInHand: ItemStack): Boolean
    {
        if (!DeathGames.running) return false

        itemStackInHand.components?.let { components ->
            val nbt = components.get(CUSTOM_DATA)?.nbt ?: return false
            if (!nbt.contains("hookMaxDistance") || !nbt.contains("hookCooldown")) return false

            val maxDistance = nbt.getDouble("hookMaxDistance")
            val cooldownSetting = nbt.getInt("hookCooldown")

            val cooldown = cooldowns.getOrPut(itemStackInHand) { Cooldown(cooldownSetting) }

            if (Shop.isInShopBounds(owner) || !cooldown.isReady()) return false

            val hitResult = owner.raycast(maxDistance, 0f, false)
            if (hitResult.type != HitResult.Type.BLOCK) return false

            val targetPos = (hitResult as BlockHitResult).blockPos.toCenterPos() + Vec3d(0.0, 1.0, 0.0)

            if (owner.pos.y > targetPos.y + 1) return false

            owner.setNoGravity(true)

            val yDistance = targetPos.y - owner.pos.y
            val xDistance = targetPos.x - owner.pos.x
            val zDistance = targetPos.z - owner.pos.z

            val (yVelocity, flightTime) = getVerticalVelocity(yDistance)
            val xVelocity = xDistance / flightTime
            val zVelocity = zDistance / flightTime

            val totalVelocity = Vec3d(xVelocity, yVelocity, zVelocity)

            val vehicle = ArmorStandEntity(world, owner.pos.x, owner.pos.y, owner.pos.z) // this entity is used for transporting the player
            vehicle.isMarker = true
            vehicle.isSilent = true
            vehicle.customName = Text.of("hook")
            vehicle.isCustomNameVisible = false
            vehicle.isInvisible = true
            vehicle.setNoGravity(true)

            world.spawnEntity(vehicle)
            activeHooks.add(ArrowHook(vehicle, owner, targetPos, totalVelocity))
            owner.startRiding(vehicle, true)

            cooldown.goOnCooldown()
            owner.itemCooldownManager.set(itemStackInHand, cooldownSetting) // 1.21.3: now using specific ItemStack

            return true
        } ?: return false
    }

    private fun getVerticalVelocity(yDistance: Double): Pair<Double, Double>
    {
        // s = 1/2 * a * t² => t = sqrt(2 * s / a)
        val flightTime = sqrt(2.0 * yDistance / GRAVITY_ACCELERATION)
        // v = a * t
        val yVelocity = GRAVITY_ACCELERATION * flightTime
        return Pair(yVelocity, flightTime)
    }

    fun isRidingHook(player: ServerPlayerEntity): Boolean = activeHooks.filter { it.owner == player }.isNotEmpty()

    /**
     * Class for tracking the arrow, which the player is riding on.
     */
    data class ArrowHook(val vehicle: ArmorStandEntity, val owner: ServerPlayerEntity, private val targetPos: Vec3d, private var velocity: Vec3d = Vec3d.ZERO)
    {
        private var previousDist = Double.MAX_VALUE
        private var recentDist = targetPos.multiply(1.0, 0.0, 1.0).subtract(this@ArrowHook.vehicle.pos.multiply(1.0, 0.0, 1.0)).length()
        fun tick()
        {
            this@ArrowHook.vehicle.setPosition(this@ArrowHook.vehicle.pos + velocity)
            velocity += Vec3d(0.0, -GRAVITY_ACCELERATION, 0.0)
            previousDist = recentDist
            recentDist = targetPos.multiply(1.0, 0.0, 1.0).subtract(this@ArrowHook.vehicle.pos.multiply(1.0, 0.0, 1.0)).length()
        }

        fun isAlive(): Boolean
        {
            if (owner.isDisconnected) return false
            return previousDist > recentDist && recentDist > 0.5 && !this@ArrowHook.vehicle.isInsideWall && !this@ArrowHook.vehicle.isOnGround
        }

        fun getEndPosition(): Vec3d = this@ArrowHook.vehicle.pos
        fun killEntity(): Unit = this@ArrowHook.vehicle.discard()
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

        fun tickDown()
        {
            if (remainingCooldown > 0) remainingCooldown--
        }

        override fun toString(): String
        {
            return "$remainingCooldown/$maxCooldown"
        }
    }
}
