package de.jagenka.gameplay.graplinghook

import de.jagenka.DeathGames
import de.jagenka.managers.PlayerManager
import de.jagenka.plus
import de.jagenka.shop.Shop
import de.jagenka.toCenterPos
import net.minecraft.core.component.DataComponents.CUSTOM_DATA
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import kotlin.jvm.optionals.getOrNull
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
                player.isNoGravity = false
            }
        }
        cooldowns.values.forEach {
            it.tickDown()
        }
        activeHooks.toList().forEach {
            if (!it.vehicle.hasExactlyOnePlayerPassenger())
            {
                it.killEntity()
                activeHooks.remove(it)
            }
            if (!it.shouldTick())
            {
                val pos = it.getEndPosition()
                val owner = it.owner
                it.killEntity()
                owner.teleportTo(pos.x, pos.y, pos.z)
                owner.isNoGravity = false
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
    fun forceTheHooker(world: Level, owner: ServerPlayer, itemStackInHand: ItemStack): Boolean
    {
        if (!DeathGames.running) return false

        itemStackInHand.components.let { components ->
            val nbt = components.get(CUSTOM_DATA)?.tag ?: return false

            val maxDistance = nbt.getDouble("hookMaxDistance").getOrNull() ?: return false
            val cooldownSetting = nbt.getInt("hookCooldown").getOrNull() ?: return false

            val cooldown = cooldowns.getOrPut(itemStackInHand) { Cooldown(cooldownSetting) }

            if (Shop.isInShopBounds(owner) || !cooldown.isReady()) return false

            // confusing name for method "raycast" with old mappings
            val hitResult = owner.pick(maxDistance, 0f, false)
            if (hitResult.type != HitResult.Type.BLOCK) return false

            val targetPos = (hitResult as BlockHitResult).blockPos.toCenterPos() + Vec3(
                0.0,
                1.0,
                0.0
            )

            if (owner.position().y > targetPos.y + 1) return false

            owner.isNoGravity = true

            val yDistance = targetPos.y - owner.position().y
            val xDistance = targetPos.x - owner.position().x
            val zDistance = targetPos.z - owner.position().z

            val (yVelocity, flightTime) = getVerticalVelocity(yDistance)
            val xVelocity = xDistance / flightTime
            val zVelocity = zDistance / flightTime

            val totalVelocity = Vec3(xVelocity, yVelocity, zVelocity)

            val vehicle = ArmorStand(
                world,
                owner.position().x,
                owner.position().y,
                owner.position().z
            ) // this entity is used for transporting the player
            vehicle.isMarker = true
            vehicle.isSilent = true
            vehicle.customName = Component.literal("hook")
            vehicle.isCustomNameVisible = false
            vehicle.isInvisible = true
            vehicle.isNoGravity = true

            world.addFreshEntity(vehicle)
            activeHooks.add(ArrowHook(vehicle, owner, targetPos, totalVelocity))
            owner.startRiding(vehicle, true, false)

            cooldown.goOnCooldown()
            owner.cooldowns.addCooldown(itemStackInHand, cooldownSetting) // 1.21.3: now using specific ItemStack

            return true
        } ?: return false
    }

    var yRotO: Float? = null
    var xRotO: Float? = null

    /**
     * experimental fun trying out shooting player, instead of letting player ride a shot entity
     */
    fun shootPlayer(serverPlayer: ServerPlayer)
    {
        // the following is basically copy pasta from arrow shooting - consider analyzing LivingEntity::knockback, as this method should move a player in a gravity arch

        val f = serverPlayer.xRot
        val g = serverPlayer.yRot // + inaccuracy in original code
        val h = 0.0
        val i = 3.0f // anderes f = 3.0f bei vollem bogen
        val j = 1.0f //anderes g = 1.0f kp das is 1

        val k =
            -Mth.sin((g * (Math.PI / 180.0).toFloat()).toDouble()) * Mth.cos((f * (Math.PI / 180.0).toFloat()).toDouble())
        val l = -Mth.sin(((f + h) * (Math.PI / 180.0).toFloat()))
        val m =
            Mth.cos((g * (Math.PI / 180.0).toFloat()).toDouble()) * Mth.cos((f * (Math.PI / 180.0).toFloat()).toDouble())

        val vec3: Vec3 = Vec3(k.toDouble(),l.toDouble(),m.toDouble()).normalize().scale(i.toDouble())
        serverPlayer.deltaMovement = vec3 // this is also essential
        serverPlayer.needsSync = true // this seems to be essential
        serverPlayer.yRot = (Mth.atan2(vec3.x, vec3.z) * 180.0f / Math.PI.toFloat()).toFloat()
        serverPlayer.xRot = (Mth.atan2(vec3.y, vec3.horizontalDistance()) * 180.0f / Math.PI.toFloat()).toFloat()
        yRotO = serverPlayer.yRot
        xRotO = serverPlayer.xRot

        val vec3_2: Vec3 = serverPlayer.getKnownMovement()
        serverPlayer.deltaMovement = serverPlayer.deltaMovement.add(vec3_2.x, if (serverPlayer.onGround()) 0.0 else vec3_2.y, vec3_2.z)
    }

    private fun getVerticalVelocity(yDistance: Double): Pair<Double, Double>
    {
        // s = 1/2 * a * t² => t = sqrt(2 * s / a)
        val flightTime = sqrt(2.0 * yDistance / GRAVITY_ACCELERATION)
        // v = a * t
        val yVelocity = GRAVITY_ACCELERATION * flightTime
        return Pair(yVelocity, flightTime)
    }

    fun isRidingHook(player: ServerPlayer): Boolean = activeHooks.any { it.owner == player }

    /**
     * Class for tracking the arrow, which the player is riding on.
     */
    data class ArrowHook(
        val vehicle: ArmorStand,
        val owner: ServerPlayer,
        private val targetPos: Vec3,
        private var velocity: Vec3 = Vec3.ZERO
    )
    {
        private var previousDist = Double.MAX_VALUE
        private var recentDist =
            targetPos.multiply(1.0, 0.0, 1.0).subtract(this@ArrowHook.vehicle.position().multiply(1.0, 0.0, 1.0))
                .length()
        fun tick()
        {
            this@ArrowHook.vehicle.setPos(this@ArrowHook.vehicle.position() + velocity)
            velocity += Vec3(0.0, -GRAVITY_ACCELERATION, 0.0)
            previousDist = recentDist
            recentDist =
                targetPos.multiply(1.0, 0.0, 1.0).subtract(this@ArrowHook.vehicle.position().multiply(1.0, 0.0, 1.0))
                    .length()
        }

        fun shouldTick(): Boolean
        {
            if (owner.hasDisconnected()) return false
            return previousDist > recentDist && recentDist > 0.5 && !this@ArrowHook.vehicle.isInWall && !this@ArrowHook.vehicle.onGround()
        }

        fun getEndPosition(): Vec3 = this@ArrowHook.vehicle.position()
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
