package de.jagenka.gameplay.graplinghook

import de.jagenka.DeathGames
import de.jagenka.shop.Shop
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents.CUSTOM_DATA
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import kotlin.jvm.optionals.getOrNull

object RubberBandHook : GrapplingHook
{
    const val HOOK_TICK_LIMIT = 200 // 10 seconds
    const val HOOK_SPEED = .08 // blocks per tick
    const val HOOK_DISENGAGE_RANGE = 3 // this value is squared

    val activeFlights = mutableListOf<FlightTask>()

    override fun tick()
    {
        val it = activeFlights.iterator()
        while (it.hasNext())
        {
            val task = it.next()
            task.tick++

            val player = task.player

            val particle = BlockParticleOption(ParticleTypes.BLOCK_MARKER, Blocks.STRUCTURE_VOID.defaultBlockState())

            player.level().sendParticles(
                particle, false, true,
                task.target.x, task.target.y, task.target.z,
                1, .0, .0, .0, .0
            )

            val delta = task.target.subtract(player.position())
            val vHook = delta.normalize().scale(HOOK_SPEED)

            player.addDeltaMovement(vHook)
            player.connection.send(ClientboundSetEntityMotionPacket(player))

            if (task.tick >= HOOK_TICK_LIMIT || delta.lengthSqr() < HOOK_DISENGAGE_RANGE)
            {
                player.isNoGravity = false

                it.remove()
            }
        }
    }

    override fun reset()
    {
        activeFlights.clear()
    }

    override fun forceTheHooker(serverPlayer: ServerPlayer, itemStackInHand: ItemStack): Boolean
    {
        if (!DeathGames.running) return false

        itemStackInHand.components.let { components ->
            val nbt = components.get(CUSTOM_DATA)?.tag ?: return false

            val maxDistance = nbt.getDouble("hookMaxDistance").getOrNull() ?: return false
            val cooldownSetting = nbt.getInt("hookCooldown").getOrNull() ?: return false

            if (Shop.isInShopBounds(serverPlayer)) return false

            val hitResult = serverPlayer.pick(maxDistance, 0f, false) // ray-cast to block
            if (hitResult.type == HitResult.Type.BLOCK && hitResult is BlockHitResult)
            {
                val targetPos = Vec3(hitResult.blockPos.x.toDouble() + .5, (hitResult.blockPos.y + 1.0), hitResult.blockPos.z.toDouble() + .5)
                launchPlayer(serverPlayer, targetPos)
            }

            serverPlayer.cooldowns.addCooldown(itemStackInHand, cooldownSetting) // 1.21.3: now using specific ItemStack

            return true
        }
    }

    private fun launchPlayer(player: ServerPlayer, target: Vec3)
    {
        player.isNoGravity = true

        player.connection.send(
            ClientboundSoundPacket(
                Holder.direct(SoundEvents.FISHING_BOBBER_THROW),
                SoundSource.PLAYERS,
                player.position().x,
                player.position().y,
                player.position().z,
                1f,
                1.2f,
                player.level().random.nextLong()
            )
        )

        activeFlights.add(
            FlightTask(
                player = player,
                target = target,
            )
        )
    }
}

data class FlightTask(
    val player: ServerPlayer,
    val target: Vec3,
    var tick: Int = 0,
)