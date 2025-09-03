package de.jagenka.gameplay.traps

import de.jagenka.BlockPos
import net.minecraft.component.DataComponentTypes.CUSTOM_DATA
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.math.Direction
import kotlin.jvm.optionals.getOrNull

object TrapManager
{
    val traps = mutableSetOf<Trap>()

    /**
     * @return  true, if custom handling is implemented here or
     *          false, if Minecraft should continue their default behavior
     */
    @JvmStatic
    fun handleTrapPlacement(ctx: ItemUsageContext): Boolean
    {
        if (ctx.side == Direction.UP)
        {
            ctx.stack.components?.let { components ->
                val nbt = components.get(CUSTOM_DATA)?.nbt ?: return false

                val trap = Trap(
                    position = BlockPos(ctx.blockPos.x, ctx.blockPos.y + 1, ctx.blockPos.z),
                    snares = nbt.getBoolean("isSnareTrap").getOrNull() ?: return false, // continue as normal, as nbt in component is invalid
                    effects = nbt.get("trapEffects", StatusEffectInstance.CODEC.listOf()).getOrNull() ?: return false,
                    triggerRange = nbt.getDouble("trapTriggerRange").getOrNull() ?: return false,
                    setupTime = nbt.getInt("trapSetupTime").getOrNull() ?: return false,
                    triggerVisibilityRange = nbt.getDouble("trapTriggerVisibilityRange").getOrNull() ?: return false,
                    visibilityRange = nbt.getDouble("trapVisibilityRange").getOrNull() ?: return false,
                    affectedRange = nbt.getDouble("trapAffectedRange").getOrNull() ?: return false,
                    triggerDuration = nbt.getInt("trapTriggerDuration").getOrNull() ?: return false,
                )

                if (trap in traps) return true // trap already exists at this location
                ctx.player?.let { player ->
                    if (player.inventory.removeStack(player.inventory.selectedSlot, 1) == ItemStack.EMPTY)
                    {
                        return false // no item in selected slot -> exit and let Minecraft handle that
                    }
                } ?: return false // no player for some reason -> let Minecraft handle that

                traps.add(trap)
                trap.startSettingUp()

                return true // custom handling implemented at this point
            } ?: return false // continue as normal, as components are empty
        }

        return false // fallback, should not happen
    }

    fun onPlayerDeath(playerName: String)
    {
        traps.forEach { trap ->
            trap.onPlayerDeath(playerName)
        }
    }

    fun removeTrap(trap: Trap)
    {
        traps.remove(trap)
    }

    fun reset()
    {
        traps.clear()
    }
}