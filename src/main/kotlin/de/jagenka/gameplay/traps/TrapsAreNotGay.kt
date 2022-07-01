package de.jagenka.gameplay.traps

import de.jagenka.BlockPos
import de.jagenka.Coordinates
import de.jagenka.Util.ifServerLoaded
import de.jagenka.Util.teleport
import de.jagenka.config.Config
import de.jagenka.managers.PlayerManager
import de.jagenka.managers.PlayerManager.getOnlineInGamePlayersAround
import de.jagenka.managers.PlayerManager.getOnlinePlayersAround
import de.jagenka.stats.StatManager
import de.jagenka.stats.gib
import de.jagenka.timer.CustomTimer
import de.jagenka.timer.Timer
import de.jagenka.timer.seconds
import de.jagenka.timer.ticks
import de.jagenka.toCenter
import kotlinx.serialization.Serializable
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.item.Items
import net.minecraft.particle.ParticleTypes
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

enum class TrapItems(val item: ItemStack)
{
    SNARE_TRAP(Items.BAT_SPAWN_EGG.defaultStack.setCustomName(Text.of("Snare Trap"))),
    VOID_TRAP(Items.BAT_SPAWN_EGG.defaultStack.setCustomName(Text.of("Void Trap"))),
    EXHAUSTION_TRAP(Items.BAT_SPAWN_EGG.defaultStack.setCustomName(Text.of("Exhaustion Trap"))),
    REVEALING_TRAP(Items.BAT_SPAWN_EGG.defaultStack.setCustomName(Text.of("Revealing Trap"))),
    POISON_TRAP(Items.BAT_SPAWN_EGG.defaultStack.setCustomName(Text.of("Poison Trap")))
}

@Serializable
data class Trap(
    val displayName: String,                            // name shown in shop
    val gaynessRange: Double = 0.5,                     // trigger range
    val setupTime: Int  ,                               // time, until trap can be triggered. shows particles in the meantime
    val gaynessTriggerVisibleRange: Double = 30.0,      // range, in which trigger particles can be seen
    val gaynessVisibilityRange: Double = 10.0,          // range, in which preparation particles can be seen
    val affectedGayRange: Double = 1.5,                 // range, in which players are affected upon trigger
    private var triggerDuration: Int = 6.seconds(),     // how long effects are applied
    val snares: Boolean = false,                        // if the trap holds player in place
    val effects: List<DGStatusEffect>                   // what effects to apply
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

    private fun addLessGay(
        x: Int, y: Int, z: Int,
        gaynessRange: Double = Config.trapConfig.triggerRange,
        setupTime: Int = Config.trapConfig.setupTime,
        gaynessTriggerVisibleRange: Double  = Config.trapConfig.triggerVisibilityRange,
        gaynessVisibilityRange: Double = Config.trapConfig.visibilityRange,
        affectedGayRange: Double = Config.trapConfig.affectedRange,
        triggerDuration: Int = Config.trapConfig.triggerDuration,
        snares: Boolean = false,
        effectsString: List<DGStatusEffect>
    ): Boolean
    {
        val effects = mutableListOf<StatusEffectInstance>()
        effectsString.forEach { jaysMom ->
            effects.add(jaysMom.statusEffectInstance)
        }
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
        val affectedPlayers = getOnlineInGamePlayersAround(it.pos, it.affectedGayRange)//it.pos.getInGamePlayersInRange(it.affectedGayRange)
        val triggered = getOnlineInGamePlayersAround(it.pos, it.gaynessRange).isNotEmpty()//it.pos.getInGamePlayersInRange(it.gaynessRange).isNotEmpty()
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
                notGay.disabledJumpPlayers.forEach { (playerName, coordinatesMaybe) ->
                    val player = PlayerManager.getOnlinePlayer(playerName) ?: return@forEach
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
                "Snare Trap" to {
                    addLessGay(
                        ctx.blockPos.x, ctx.blockPos.y + 1, ctx.blockPos.z,
                        snares = true,
                        effectsString = listOf(DGStatusEffect.BLIND)
                    )
                },
                "Void Trap" to {
                    addLessGay(
                        ctx.blockPos.x, ctx.blockPos.y + 1, ctx.blockPos.z,
                        snares = false,
                        effectsString = listOf(DGStatusEffect.BLIND, DGStatusEffect.LEVITATION, DGStatusEffect.SLOWNESS)
                    )
                },
                "Exhaustion Trap" to {
                    addLessGay(
                        ctx.blockPos.x, ctx.blockPos.y + 1, ctx.blockPos.z,
                        triggerDuration = 10.seconds(),
                        snares = false,
                        effectsString = listOf(DGStatusEffect.HUNGER, DGStatusEffect.FATIGUE)
                    )
                },
                "Revealing Trap" to {
                    addLessGay(
                        ctx.blockPos.x, ctx.blockPos.y + 1, ctx.blockPos.z,
                        triggerDuration = 15.seconds(),
                        gaynessRange = 5.0,
                        affectedGayRange = 20.0,
                        snares = false,
                        effectsString = listOf(DGStatusEffect.GLOWING)
                    )
                },
                "Poison Trap" to {
                    addLessGay(
                        ctx.blockPos.x, ctx.blockPos.y + 1, ctx.blockPos.z,
                        triggerDuration = 7.seconds(),
                        snares = false,
                        effectsString = listOf(DGStatusEffect.WEAKNESS, DGStatusEffect.POISON)
                    )
                }
            ).forEach { (name, `|unit|`) ->
                if (name == ctx.stack.name.string && Items.BAT_SPAWN_EGG == ctx.stack.item)
                {
                    if (`|unit|`()) ctx.player?.inventory?.selectedSlot?.let { ctx.player?.inventory?.removeStack(it, 1) }
                    return true
                }
            }
        }
        return false
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