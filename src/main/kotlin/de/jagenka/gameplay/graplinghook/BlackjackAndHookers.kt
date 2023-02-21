package de.jagenka.gameplay.graplinghook

import de.jagenka.minus
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.entity.projectile.PersistentProjectileEntity
import net.minecraft.world.World
import kotlin.math.pow

object BlackjackAndHookers
{
    const val TICKS_TO_TARGET = 40

    @JvmStatic
    fun forceTheHooker(world: World, owner: PlayerEntity): Boolean
    {

        owner.fishHook?.let { bobber ->
            if (!bobber.isOnGround) return false
            val direction = (bobber.pos - owner.pos).normalize()

            //val xzVector = Vec3d(bobber.pos.x - owner.pos.x, 0.0, bobber.pos.z - owner.pos.z)
            //val xzL = xzVector.length()
            //val xzVelocity = xzL / TIME_TO_TARGET
            val arrow = ArrowEntity(world, owner)
            arrow.setVelocity(0.0, 0.5, 0.0)
            arrow.damage = 0.0
            arrow.pickupType = PersistentProjectileEntity.PickupPermission.DISALLOWED
            arrow.pierceLevel = 16
            //arrow.isSilent = true
            arrow.isInvisible = true
            world.spawnEntity(arrow)
            //owner.startRiding(arrow)

            val targetHeight = bobber.pos.y - owner.pos.y
            // d(N) = y0 + 50 * (vt0 + 3.92) * (1 - 0.98^N) - 3.92 * N => vt0 = (d(N) - y0 + 3.92 * N) / (50 * (1 - 0.98^N)) - 3.92
            val yVelocity = (targetHeight + 3.92 * TICKS_TO_TARGET) / (50.0 * (1 - 0.98.pow(TICKS_TO_TARGET))) - 3.92

            //owner.velocity = Vec3d(0.0, yVelocity, 0.0)
            //owner.velocityModified = true
        } ?: return false
        return true
    }
}
