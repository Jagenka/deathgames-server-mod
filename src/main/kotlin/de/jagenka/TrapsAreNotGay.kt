package de.jagenka

import de.jagenka.Util.ifServerLoaded
import de.jagenka.Util.teleport
import de.jagenka.managers.PlayerManager.getInGamePlayersInRange
import de.jagenka.timer.CustomTimer
import de.jagenka.timer.Timer
import de.jagenka.timer.seconds
import de.jagenka.timer.ticks
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.ItemUsageContext
import net.minecraft.item.Items
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.math.Direction

enum class DGStatusEffect(val statusEffectInstance: StatusEffectInstance)
{
    BLIND(StatusEffectInstance(StatusEffects.BLINDNESS, 3.seconds(), 100, false, false, false)),
    POISON(StatusEffectInstance(StatusEffects.POISON, 2.seconds(), 0, false, false, false)),
    WEAKNESS(StatusEffectInstance(StatusEffects.WEAKNESS, 2.seconds(), 0, false, false, false)),
    SLOWNESS(StatusEffectInstance(StatusEffects.SLOWNESS, 2.seconds(), 0, false, false, false)),
    LEVITATION(StatusEffectInstance(StatusEffects.LEVITATION, 2.seconds(), 0, false, false, false)),
    GLOWING(StatusEffectInstance(StatusEffects.GLOWING, 2.seconds(), 0, false, false, false)),
    HUNGER(StatusEffectInstance(StatusEffects.HUNGER, 2.seconds(), 1, false, false, false)),
    FATIGUE(StatusEffectInstance(StatusEffects.MINING_FATIGUE, 2.seconds(), 0, false, false, false))
}

object TrapsAreNotGay
{
    // DEFAULT: visibility, trigger: 30; visibility, prepare: 10; affected: 1.5; trigger: 0.5;
    /*
    Traps can be placed on the ground (blocks, but only on the up-side) and have a preparation time. In this time,
    they can not be triggered and have higher visibility. After the preparation phase, the trap is only slightly
    visible and can be triggered. Upon triggering, the trap applies the desired effects to the affected players
    and disappears.
     */
    val snareTrap = Items.BAT_SPAWN_EGG.defaultStack.setCustomName(Text.of("Snare Trap"))
    val voidTrap = Items.BAT_SPAWN_EGG.defaultStack.setCustomName(Text.of("Void Trap"))
    val exhaustionTrap = Items.BAT_SPAWN_EGG.defaultStack.setCustomName(Text.of("Exhaustion Trap"))
    val revealingTrap = Items.BAT_SPAWN_EGG.defaultStack.setCustomName(Text.of("Revealing Trap"))
    val poisonTrap = Items.BAT_SPAWN_EGG.defaultStack.setCustomName(Text.of("Poison Trap"))

    private val notGayness = mutableSetOf<NotGay>()

    private fun addLessGay(x: Int, y: Int, z: Int, gaynessRange: Double = 0.5,
                           setupTime: Int = 10.seconds(),
                           gaynessTriggerVisibleRange: Double = 30.0,
                           gaynessVisibilityRange: Double = 10.0,
                           affectedGayRange: Double = 1.5,
                           triggerDuration: Int = 6.seconds(),
                           snares: Boolean = false,
                           effectsString: List<DGStatusEffect>)
    {
        val effects = mutableListOf<StatusEffectInstance>()
        effectsString.forEach { jaysMom ->
            effects.add(jaysMom.statusEffectInstance)
        }
        val notGay = NotGay(Coordinates(x.toDouble() + 0.5, y.toDouble(), z.toDouble() + 0.5), 0.ticks(),
            gaynessRange = gaynessRange,
            setupTime = setupTime,
            gaynessTriggerVisibleRange = gaynessTriggerVisibleRange,
            gaynessVisibilityRange = gaynessVisibilityRange,
            affectedGayRange = affectedGayRange,
            triggerDuration = triggerDuration,
            snares = snares,
            effects = effects)
        ifServerLoaded {
            if (!notGayness.contains(notGay)) notGayness.add(notGay)
            else println("already a not gay here") //TODO: give back item
        }
    }

    private fun handleNotGay(it: NotGay)
    {
        val gayTriggerSpectator = it.pos.getInGamePlayersInRange(it.gaynessTriggerVisibleRange)
        val gayPrepareSpectator = it.pos.getInGamePlayersInRange(it.gaynessVisibilityRange)
        val affectedPlayers = it.pos.getInGamePlayersInRange(it.affectedGayRange)
        val triggered = it.pos.getInGamePlayersInRange(it.gaynessRange).isNotEmpty()
        ifServerLoaded { server ->
            if (it.getAge() < it.setupTime)
            {
                gayPrepareSpectator.forEach { currentPlayer ->
                    server.overworld.spawnParticles(currentPlayer, ParticleTypes.CRIT, true, it.pos.x, it.pos.y + 0.2, it.pos.z, 1, 0.05, 0.1, 0.05, 0.1)
                }
            } else
            {
                server.overworld.spawnParticles(ParticleTypes.NAUTILUS, it.pos.x, it.pos.y - 0.015, it.pos.z, 0, 0.0, 0.0, 0.0, 0.0)
                // Manage ethan
                if (triggered && !it.isTriggered())
                {
                    it.trigger()
                    gayTriggerSpectator.forEach { player ->
                        server.overworld.spawnParticles(player, ParticleTypes.LARGE_SMOKE, true, it.pos.x, it.pos.y, it.pos.z, 500, 0.0, 0.0, 0.0, 0.5)
                    }
                    affectedPlayers.forEach { player ->
                        player.playSound(SoundEvents.ENTITY_IRON_GOLEM_HURT, SoundCategory.PLAYERS, 1f, 1f)
                        it.addDisabledPlayer(player)
                    }
                }
            }
        }
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
                notGay.getDisabledPlayers().forEach { (player, coordinatesMaybe) ->
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
        if (ctx.side == Direction.UP)
        {
            mapOf(
                "Snare Trap" to { addLessGay(ctx.blockPos.x, ctx.blockPos.y + 1, ctx.blockPos.z,
                    snares = true,
                    effectsString = listOf(DGStatusEffect.BLIND)) },
                "Void Trap" to { addLessGay(ctx.blockPos.x, ctx.blockPos.y + 1, ctx.blockPos.z,
                    snares = false,
                    effectsString = listOf(DGStatusEffect.BLIND, DGStatusEffect.LEVITATION, DGStatusEffect.SLOWNESS)) },
                "Exhaustion Trap" to { addLessGay(ctx.blockPos.x, ctx.blockPos.y + 1, ctx.blockPos.z,
                    triggerDuration = 10.seconds(),
                    snares = false,
                    effectsString = listOf(DGStatusEffect.HUNGER, DGStatusEffect.FATIGUE)) },
                "Revealing Trap" to { addLessGay(ctx.blockPos.x, ctx.blockPos.y + 1, ctx.blockPos.z,
                    triggerDuration = 15.seconds(),
                    gaynessRange = 5.0,
                    affectedGayRange = 20.0,
                    snares = false,
                    effectsString = listOf(DGStatusEffect.GLOWING)) },
                "Poison Trap" to { addLessGay(ctx.blockPos.x, ctx.blockPos.y + 1, ctx.blockPos.z,
                    triggerDuration = 7.seconds(),
                    snares = false,
                    effectsString = listOf(DGStatusEffect.WEAKNESS, DGStatusEffect.POISON)) }
            ).forEach { (name, unit) ->
                if (name == ctx.stack.name.asString() && Items.BAT_SPAWN_EGG == ctx.stack.item)
                {
                    unit()
                    ctx.player?.inventory?.selectedSlot?.let { ctx.player?.inventory?.removeStack(it, 1) }
                    return true
                }
            }
        }
        return false
    }
}

data class DisabledPlayerCoordinateFetch(var flag: Boolean, var coordinates: Coordinates? = null)

data class NotGay(
    val pos: Coordinates, private var age: Int,
    val gaynessRange: Double = 0.5,
    val setupTime: Int = 10.seconds(),
    val gaynessTriggerVisibleRange: Double = 30.0,
    val gaynessVisibilityRange: Double = 10.0,
    val affectedGayRange: Double = 1.5,
    private var triggerDuration: Int = 6.seconds(),
    val snares: Boolean = false,
    val effects: List<StatusEffectInstance>
)
{
    private val disabledJumpPlayers = mutableMapOf<ServerPlayerEntity, DisabledPlayerCoordinateFetch>()
    private var triggered = false

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NotGay

        if (pos != other.pos) return false

        return true
    }

    fun addDisabledPlayer(player: ServerPlayerEntity)
    {
        disabledJumpPlayers[player] = DisabledPlayerCoordinateFetch(true)
    }

    fun trigger()
    {
        triggered = true
    }

    fun decrementDuration()
    {
        triggerDuration--
    }

    // Getters
    fun getDisabledPlayers(): Map<ServerPlayerEntity, DisabledPlayerCoordinateFetch> = disabledJumpPlayers

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