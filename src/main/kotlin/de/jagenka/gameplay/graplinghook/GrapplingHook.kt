package de.jagenka.gameplay.graplinghook

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

interface GrapplingHook
{
    fun tick()

    fun reset()

    fun forceTheHooker(serverPlayer: ServerPlayer, itemStackInHand: ItemStack): Boolean

    companion object
    {
        val itemItem: Item = Items.CARROT_ON_A_STICK

        fun getDefault(): GrapplingHook = RubberBandHook
    }
}