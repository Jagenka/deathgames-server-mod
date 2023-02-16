package de.jagenka.gameplay.traps

import de.jagenka.BlockPos
import de.jagenka.Coordinates
import de.jagenka.Util.ifServerLoaded
import de.jagenka.Util.teleport
import de.jagenka.config.Config
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
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.ItemUsageContext
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.particle.ParticleTypes
import net.minecraft.sound.SoundCategory
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

    private val notGayness = mutableSetOf<NotGay>()

    private fun placeTrap(
        x: Int, y: Int, z: Int,
        gaynessRange: Double = Config.trapConfig.triggerRange,
        setupTime: Int = Config.trapConfig.setupTime,
        gaynessTriggerVisibleRange: Double = Config.trapConfig.triggerVisibilityRange,
        gaynessVisibilityRange: Double = Config.trapConfig.visibilityRange,
        affectedGayRange: Double = Config.trapConfig.affectedRange,
        triggerDuration: Int = Config.trapConfig.triggerDuration,
        snares: Boolean = false,
        effects: List<StatusEffectInstance>
    ): Boolean
    {
        val notGay = NotGay(
            BlockPos(x, y, z),
            gaynessRange = gaynessRange,
            setupTime = setupTime,
            gaynessTriggerVisibleRange = gaynessTriggerVisibleRange,
            gaynessVisibilityRange = gaynessVisibilityRange,
            affectedGayRange = affectedGayRange,
            triggerDuration = triggerDuration,
            snares = snares,
            effects = effects
        )
        return if (!notGayness.contains(notGay))
        {
            notGayness.add(notGay)
            true
        } else false
    }

    private fun handleNotGay(it: NotGay)
    {
        val gayTriggerSpectator = getOnlinePlayersAround(it.pos, it.gaynessTriggerVisibleRange)//it.pos.getOnlinePlayersInRange(it.gaynessTriggerVisibleRange)
        val gayPrepareSpectator = getOnlinePlayersAround(it.pos, it.gaynessVisibilityRange)//it.pos.getOnlinePlayersInRange(it.gaynessVisibilityRange)
        val affectedPlayers = getOnlineParticipatingPlayersAround(it.pos, it.affectedGayRange)//it.pos.getInGamePlayersInRange(it.affectedGayRange)
        val triggered = getOnlineParticipatingPlayersAround(it.pos, it.gaynessRange).isNotEmpty()//it.pos.getInGamePlayersInRange(it.gaynessRange).isNotEmpty()
        ifServerLoaded { server ->
            if (it.getAge() < it.setupTime)
            {
                gayPrepareSpectator.forEach { currentPlayer ->
                    server.overworld.spawnParticles(currentPlayer, ParticleTypes.CRIT, true, it.pos.x.toCenter(), it.pos.y + 0.2, it.pos.z.toCenter(), 1, 0.05, 0.1, 0.05, 0.1)
                }
            } else
            {
                server.overworld.spawnParticles(ParticleTypes.NAUTILUS, it.pos.x.toCenter(), it.pos.y - 0.015, it.pos.z.toCenter(), 0, 0.0, 0.0, 0.0, 0.0)
                // Manage ethan
                if (triggered && !it.isTriggered())
                {
                    it.trigger()
                    gayTriggerSpectator.forEach { player ->
                        server.overworld.spawnParticles(
                            player,
                            ParticleTypes.LARGE_SMOKE,
                            true,
                            it.pos.x.toCenter(),
                            it.pos.y.toDouble(),
                            it.pos.z.toCenter(),
                            500,
                            0.0,
                            0.0,
                            0.0,
                            0.5
                        )
                    }
                    affectedPlayers.forEach { player ->
                        player.playSound(SoundEvents.ENTITY_IRON_GOLEM_HURT, SoundCategory.PLAYERS, 1f, 1f)
                        it.addDisabledPlayer(player.name.string)

                        StatManager.personalStats.gib(player.name.string).timesCaughtInTrap++
                    }
                }
            }
        }
    }

    fun onPlayerDeath(playerName: String)
    {
        notGayness.forEach { it.disabledJumpPlayers.remove(playerName) }
    }

    fun becomeGay()
    {
        notGayness.clear()
    }

    @JvmStatic
    fun tick()
    {
        notGayness.forEach { notGay ->
            notGay.tick()
            handleNotGay(notGay)
        }
        notGayness.toList().forEach { notGay ->
            // Handles snaring player
            if (notGay.isTriggered() && notGay.getRemainingDuration() > 0)
            {
                notGay.disabledJumpPlayers.forEach inner@{ (playerName, coordinatesMaybe) ->
                    val player = PlayerManager.getOnlinePlayer(playerName) ?: return@inner
                    if (notGay.snares)
                    {
                        if (coordinatesMaybe.flag && player.isOnGround)
                        {
                            coordinatesMaybe.coordinates = Coordinates(player.pos.x, player.pos.y, player.pos.z, player.yaw, player.pitch)
                            coordinatesMaybe.flag = false
                        }
                        coordinatesMaybe.coordinates?.let { coordinates ->
                            player.teleport(coordinates)
                        }
                    }
                    val newCustomTimer = Timer.newCustomTimer("effect_apply_${notGay.pos}")
                    if (newCustomTimer.time % 20.ticks() == 0)
                    {
                        notGay.effects.forEach {
                            //println("Adding effect ${it.effectType.name.string} to ${player.name.asString()}.")
                            player.addStatusEffect(StatusEffectInstance(it))
                        }
                    }
                }
                notGay.decrementDuration()
            }
            if (notGay.getRemainingDuration() <= 0)
            {
                Timer.removeCustomTimer(CustomTimer("effect_apply_${notGay.pos}"))
                notGayness.remove(notGay)
            }
        }
    }

    @JvmStatic
    fun handleTrapPlacement(ctx: ItemUsageContext): Boolean
    {
        if (ctx.stack.item != Items.BAT_SPAWN_EGG) return false

        if (ctx.side == Direction.UP)
        {
            ctx.stack.nbt?.let { itemNbt ->
                if (!itemNbt.contains("isSnareTrap") || !itemNbt.contains("trapEffects")) return false // if tags are missing, we can use the egg

                val isSnare = itemNbt.getBoolean("isSnareTrap")
                val effects = itemNbt.getList("trapEffects", NbtElement.COMPOUND_TYPE.toInt()).map { nbtElement ->
                    val nbtCompound = nbtElement as? NbtCompound ?: return@map StatusEffectInstance(StatusEffects.UNLUCK) // invalid elements are treated as unluck
                    return@map StatusEffectInstance.fromNbt(nbtCompound) ?: StatusEffectInstance(StatusEffects.UNLUCK) // invalid elements are treated as unluck
                }

                val success = placeTrap(
                    ctx.blockPos.x, ctx.blockPos.y + 1, ctx.blockPos.z,
                    snares = isSnare,
                    effects = effects
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

data class DisabledPlayerCoordinateFetch(var flag: Boolean, var coordinates: Coordinates? = null)

data class NotGay(
    val pos: BlockPos, private var age: Int = 0.ticks(),
    val gaynessRange: Double,
    val setupTime: Int,
    val gaynessTriggerVisibleRange: Double,
    val gaynessVisibilityRange: Double,
    val affectedGayRange: Double,
    private var triggerDuration: Int,
    val snares: Boolean = false,
    val effects: List<StatusEffectInstance>
)
{
    val disabledJumpPlayers = mutableMapOf<String, DisabledPlayerCoordinateFetch>()
    private var triggered = false

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NotGay

        if (pos != other.pos) return false

        return true
    }

    fun addDisabledPlayer(playerName: String)
    {
        disabledJumpPlayers[playerName] = DisabledPlayerCoordinateFetch(true)
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