package de.jagenka.gameplay.traps

import de.jagenka.BlockPos
import de.jagenka.Coordinates
import de.jagenka.Util.ifServerLoaded
import de.jagenka.Util.teleport
import de.jagenka.managers.PlayerManager
import de.jagenka.managers.PlayerManager.getOnlineParticipatingPlayersAround
import de.jagenka.managers.PlayerManager.getOnlinePlayersAround
import de.jagenka.stats.StatManager
import de.jagenka.stats.gib
import de.jagenka.timer.CustomTimer
import de.jagenka.timer.Timer
import de.jagenka.timer.seconds
import de.jagenka.timer.ticks
import de.jagenka.toCenter
import net.minecraft.component.DataComponentTypes.CUSTOM_DATA
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.ItemUsageContext
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.particle.ParticleTypes
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Direction

/*
{Ambient:0b,Amplifier:100b,Duration:60,Id:15,ShowIcon:0b,ShowParticles:0b} blind
{Ambient:0b,Amplifier:0b,Duration:40,Id:19,ShowIcon:0b,ShowParticles:0b} poison
{Ambient:0b,Amplifier:0b,Duration:40,Id:18,ShowIcon:0b,ShowParticles:0b} weakness
{Ambient:0b,Amplifier:0b,Duration:40,Id:2,ShowIcon:0b,ShowParticles:0b} slowness
{Ambient:0b,Amplifier:0b,Duration:40,Id:25,ShowIcon:0b,ShowParticles:0b} levitation
{Ambient:0b,Amplifier:0b,Duration:40,Id:24,ShowIcon:0b,ShowParticles:0b} glowing
{Ambient:0b,Amplifier:1b,Duration:40,Id:17,ShowIcon:0b,ShowParticles:0b} hunger
{Ambient:0b,Amplifier:0b,Duration:40,Id:4,ShowIcon:0b,ShowParticles:0b} fatigue
 */

data class Trap(
    val displayName: String,                            // name shown in shop
    val gaynessRange: Double = 0.5,                     // trigger range
    val setupTime: Int,                               // time, until trap can be triggered. shows particles in the meantime
    val gaynessTriggerVisibleRange: Double = 30.0,      // range, in which trigger particles can be seen
    val gaynessVisibilityRange: Double = 10.0,          // range, in which preparation particles can be seen
    val affectedGayRange: Double = 1.5,                 // range, in which players are affected upon trigger
    private var triggerDuration: Int = 6.seconds(),     // how long effects are applied
    val snares: Boolean = false,                        // if the trap holds player in place
    val effects: List<StatusEffectInstance>                   // what effects to apply
)

object TrapsAreNotGay
{
    // DEFAULT: visibility, trigger: 30; visibility, prepare: 10; affected: 1.5; trigger: 0.5;
    /*
    Traps can be placed on the ground (blocks, but only on the up-side) and have a preparation time. In this time,
    they can not be triggered and have higher visibility. After the preparation phase, the trap is only slightly
    visible and can be triggered. Upon triggering, the trap applies the desired effects to the affected players
    and disappears.
     */

    private val allTraps = mutableSetOf<NotGay>()

    private fun placeTrap(
        x: Int, y: Int, z: Int,
        triggerRange: Double,
        setupTime: Int,
        triggerVisibilityRange: Double,
        visibilityRange: Double,
        affectedRange: Double,
        triggerDuration: Int,
        snares: Boolean = false,
        effects: List<StatusEffectInstance>
    ): Boolean
    {
        val notGay = NotGay(
            BlockPos(x, y, z),
            triggerRange = triggerRange,
            setupTime = setupTime,
            triggerVisibilityRange = triggerVisibilityRange,
            visibilityRange = visibilityRange,
            affectedRange = affectedRange,
            triggerDuration = triggerDuration,
            snares = snares,
            effects = effects
        )
        return if (!allTraps.contains(notGay))
        {
            allTraps.add(notGay)
            true
        } else false
    }

    /**
     * @param it is one trap - thanks @Runebreaker for this perfect class name
     */
    private fun handleEffects(it: NotGay)
    {
        val playersInTriggerVisibilityRange = getOnlinePlayersAround(it.pos, it.triggerVisibilityRange)
        val playersInGeneralVisibilityRange = getOnlinePlayersAround(it.pos, it.visibilityRange)
        val playersHitByTrapEffects = getOnlineParticipatingPlayersAround(it.pos, it.affectedRange)
        val trapTriggered = getOnlineParticipatingPlayersAround(it.pos, it.triggerRange).isNotEmpty()
        ifServerLoaded { server ->
            if (it.getAge() < it.setupTime)
            {
                playersInGeneralVisibilityRange.forEach { currentPlayer ->
                    server.overworld.spawnParticles(currentPlayer, ParticleTypes.CRIT, true, it.pos.x.toCenter(), it.pos.y + 0.2, it.pos.z.toCenter(), 1, 0.05, 0.1, 0.05, 0.1)
                }
            } else
            {
                server.overworld.spawnParticles(ParticleTypes.NAUTILUS, it.pos.x.toCenter(), it.pos.y - 0.015, it.pos.z.toCenter(), 0, 0.0, 0.0, 0.0, 0.0)
                // Manage ethan
                if (trapTriggered && !it.isTriggered())
                {
                    it.trigger()
                    playersInTriggerVisibilityRange.forEach { player ->
                        server.overworld.spawnParticles(
                            /* viewer = */ player,
                            /* particle = */ ParticleTypes.LARGE_SMOKE,
                            /* force = */ true,
                            /* x = */ it.pos.x.toCenter(),
                            /* y = */ it.pos.y.toDouble(),
                            /* z = */ it.pos.z.toCenter(),
                            /* count = */ 500,
                            /* deltaX = */ 0.0,
                            /* deltaY = */ 0.0,
                            /* deltaZ = */ 0.0,
                            /* speed = */ 0.5
                        )
                    }
                    playersHitByTrapEffects.forEach { player ->
                        player.playSound(SoundEvents.ENTITY_IRON_GOLEM_HURT, 1f, 1f)
                        it.addTrappedPlayer(player.name.string)
                        StatManager.personalStats.gib(player.name.string).timesCaughtInTrap++

                        println("trigger")
                    }
                }
            }
        }
    }

    fun onPlayerDeath(playerName: String)
    {
        allTraps.forEach { it.trappedPlayers.remove(playerName) }
    }

    fun becomeGay()
    {
        allTraps.clear()
    }

    @JvmStatic
    fun tick()
    {
        allTraps.forEach { notGay ->
            notGay.tick()
            handleEffects(notGay)
        }
        allTraps.toList().forEach { trap ->
            // handles snaring player
            if (trap.isTriggered() && trap.getRemainingDuration() > 0)
            {
                trap.trappedPlayers.forEach inner@{ (playerName, coordinatesMaybe) ->
                    val player = PlayerManager.getOnlinePlayer(playerName) ?: return@inner
                    if (trap.snares)
                    {
                        if (coordinatesMaybe.snared && player.isOnGround)
                        {
                            coordinatesMaybe.coordinates = Coordinates(player.pos.x, player.pos.y, player.pos.z, player.yaw, player.pitch)
                            coordinatesMaybe.snared = false
                        }
                        coordinatesMaybe.coordinates?.let { coordinates ->
                            player.teleport(coordinates)
                        }
                    }
                    // only one trap per position -> pos can be identifier
                    val newCustomTimer = Timer.newCustomTimer("effect_apply_${trap.pos}")

                    // handles potions effects
                    if (newCustomTimer.time % 20.ticks() == 0)
                    {
                        trap.effects.forEach {
                            player.addStatusEffect(StatusEffectInstance(it))
                        }
                    }
                }
                trap.decrementDuration()
            }
            // handles removing potions effects
            if (trap.getRemainingDuration() <= 0)
            {
                Timer.removeCustomTimer(CustomTimer("effect_apply_${trap.pos}"))
                allTraps.remove(trap)
            }
        }
    }

    @JvmStatic
    fun handleTrapPlacement(ctx: ItemUsageContext): Boolean
    {
        if (ctx.side == Direction.UP)
        {
            ctx.stack.components?.let { components ->
                val nbt = components.get(CUSTOM_DATA)?.nbt ?: return false

                if (!nbt.contains("isSnareTrap") || !nbt.contains("trapEffects") || !nbt.contains("trapTriggerRange")
                    || !nbt.contains("trapSetupTime") || !nbt.contains("trapTriggerVisibilityRange")
                    || !nbt.contains("trapVisibilityRange") || !nbt.contains("trapAffectedRange")
                    || !nbt.contains("trapTriggerDuration")
                ) return false // if tags are missing, we can use the egg

                val isSnare = nbt.getBoolean("isSnareTrap")
                val effects = nbt.getList("trapEffects", NbtElement.COMPOUND_TYPE.toInt()).map { nbtElement ->
                    val nbtCompound = nbtElement as? NbtCompound ?: return@map StatusEffectInstance(StatusEffects.UNLUCK) // invalid elements are treated as unluck
                    return@map StatusEffectInstance.fromNbt(nbtCompound) ?: StatusEffectInstance(StatusEffects.UNLUCK) // invalid elements are treated as unluck
                }
                val triggerRange = nbt.getDouble("trapTriggerRange")
                val setupTime = nbt.getInt("trapSetupTime")
                val triggerVisibilityRange = nbt.getDouble("trapTriggerVisibilityRange")
                val visibilityRange = nbt.getDouble("trapVisibilityRange")
                val affectedRange = nbt.getDouble("trapAffectedRange")
                val triggerDuration = nbt.getInt("trapTriggerDuration")

                val success = placeTrap(
                    ctx.blockPos.x, ctx.blockPos.y + 1, ctx.blockPos.z,
                    snares = isSnare,
                    effects = effects,
                    triggerRange = triggerRange,
                    setupTime = setupTime,
                    triggerVisibilityRange = triggerVisibilityRange,
                    visibilityRange = visibilityRange,
                    affectedRange = affectedRange,
                    triggerDuration = triggerDuration
                )

                if (success)
                {
                    ctx.player?.inventory?.selectedSlot?.let { ctx.player?.inventory?.removeStack(it, 1) }
                }

            } ?: return false // if there is no nbt, we can use the egg
        }

        return true // cancel bat spawn egg placement if it is a trap
    }
}

data class TrappedPlayerCoordinateFetch(var snared: Boolean, var coordinates: Coordinates? = null)

class NotGay(
    val pos: BlockPos, private var age: Int = 0.ticks(),
    val triggerRange: Double,
    val setupTime: Int,
    val triggerVisibilityRange: Double,
    val visibilityRange: Double,
    val affectedRange: Double,
    private var triggerDuration: Int,
    val snares: Boolean = false,
    val effects: List<StatusEffectInstance>
)
{
    /**
     * keys are playerNames
     */
    val trappedPlayers = mutableMapOf<String, TrappedPlayerCoordinateFetch>()
    private var triggered = false

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NotGay

        return pos == other.pos
    }

    fun addTrappedPlayer(playerName: String)
    {
        trappedPlayers[playerName] = TrappedPlayerCoordinateFetch(true)
    }

    fun trigger()
    {
        triggered = true
    }

    fun decrementDuration()
    {
        triggerDuration--
    }

    fun isTriggered(): Boolean = triggered

    fun getRemainingDuration(): Int = triggerDuration

    fun getAge(): Int = this.age

    override fun hashCode(): Int
    {
        return pos.hashCode()
    }

    fun tick()
    {
        age++
    }
}