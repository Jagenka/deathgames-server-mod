package de.jagenka.gameplay.graplinghook

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

abstract class GrapplingHook
{
    abstract val maxDistance: Double
    abstract val cooldownSetting: Int

    abstract fun tick()

    abstract fun reset()

    abstract fun forceTheHooker(serverPlayer: ServerPlayer, itemStackInHand: ItemStack): Boolean

    abstract fun cancelFlight(serverPlayer: ServerPlayer)

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GrapplingHook

        if (maxDistance != other.maxDistance) return false
        if (cooldownSetting != other.cooldownSetting) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = maxDistance.hashCode()
        result = 31 * result + cooldownSetting
        return result
    }


    companion object
    {
        val itemItem: Item = Items.CARROT_ON_A_STICK
        val defaultType: Type = Type.RUBBER_BAND

        /**
         * stores all available hooks. note, that one hook simulates multiple players.
         * just parameters can be different between hooks
         */
        val usableHooks = mutableSetOf<GrapplingHook>()

        fun getOrCreate(maxDistance: Double, cooldownSetting: Int, type: Type = Type.RUBBER_BAND): GrapplingHook
        {
            val hook = usableHooks.find {
                when (type)
                {
                    Type.LEGACY -> it is BlackjackAndHookers
                    Type.RUBBER_BAND -> it is RubberBandHook
                } &&
                        it.cooldownSetting == cooldownSetting &&
                        it.maxDistance == maxDistance
            }
            if (hook != null)
            {
                return hook
            } else
            {
                val newHook = when (type)
                {
                    Type.LEGACY -> BlackjackAndHookers(maxDistance, cooldownSetting)
                    Type.RUBBER_BAND -> RubberBandHook(maxDistance, cooldownSetting)
                }
                usableHooks.add(newHook)
                return newHook
            }
        }
    }

    enum class Type
    {
        LEGACY, RUBBER_BAND
    }
}