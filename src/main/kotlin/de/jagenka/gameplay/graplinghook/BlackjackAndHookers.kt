package de.jagenka.gameplay.graplinghook

import de.jagenka.DeathGames
import de.jagenka.managers.ShopManager.isInShopBounds
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class BlackjackAndHookers(override val maxDistance: Double, override val cooldownSetting: Int) : GrapplingHook()
{
    override fun tick()
    {

    }

    override fun reset()
    {

    }

    override fun forceTheHooker(serverPlayer: ServerPlayer, itemStackInHand: ItemStack): Boolean
    {
        if (!DeathGames.running) return false
        if (serverPlayer.isInShopBounds) return false

        determineMovementAndMove(serverPlayer, maxDistance)

        serverPlayer.cooldowns.addCooldown(itemStackInHand, cooldownSetting) // 1.21.3: now using specific ItemStack

        return true

    }

    override fun cancelFlight(serverPlayer: ServerPlayer)
    {
        // nothing to do
    }

    private fun determineMovementAndMove(serverPlayer: ServerPlayer, maxDistance: Double = 20.0)
    {
        val hitResult = serverPlayer.pick(maxDistance, 0f, false) // raycast to block

        when (hitResult.type)
        {
            HitResult.Type.BLOCK ->
            {
                // player wants to land ON the block they targeted
                // idea: maybe a little block-inwards?
                val targetPos = Vec3(hitResult.location.x, (hitResult.location.y + 0.5).roundToInt().toDouble(), hitResult.location.z)
                val delta = targetPos.subtract(serverPlayer.position())

                // constant
                val k = 2 // arcing factor
                val airDeceleration = 0.09 // air resistance

                val vY = sqrt(2 * serverPlayer.gravity * ((delta.y / 2) + k))
                val flightTime = (vY + sqrt(vY.pow(2) - 2 * serverPlayer.gravity * delta.y)) / serverPlayer.gravity
                val vX = (delta.x * airDeceleration) / (1 - (1 - airDeceleration).pow(flightTime))
                val vZ = (delta.z * airDeceleration) / (1 - (1 - airDeceleration).pow(flightTime))

                val v = Vec3(vX, vY, vZ).scale(1.2)

                serverPlayer.moveAndUpdate(v)
            }

            else -> // just yeet in that direction
            {
                val v = serverPlayer.getViewVector(1.0f)
                serverPlayer.addEffect(MobEffectInstance(MobEffects.SLOW_FALLING, 40))
                serverPlayer.moveAndUpdate(v)
            }
        }
    }

    private fun ServerPlayer.moveAndUpdate(movement: Vec3)
    {
        this.addDeltaMovement(movement)
        this.connection.send(ClientboundSetEntityMotionPacket(this))
    }
}
