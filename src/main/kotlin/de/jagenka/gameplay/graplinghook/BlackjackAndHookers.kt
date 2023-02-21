package de.jagenka.gameplay.graplinghook

import de.jagenka.minus
import de.jagenka.times
import net.minecraft.item.ItemUsageContext
import net.minecraft.item.Items

object BlackjackAndHookers
{
    @JvmStatic
    fun forceTheHooker(strength: Double, ctx: ItemUsageContext): Boolean
    {
        ctx.player?.let { owner ->
            owner.fishHook?.let { bobber ->
                if (!bobber.isOnGround) return false
                val direction = (bobber.pos - owner.pos).normalize()
                owner.addVelocity(direction * strength)
            } ?: return false
        } ?: return false
        return true
    }
}
