package de.jagenka.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CompassItem.class)
public class CompassItemMixin
{
    @Inject(method = "inventoryTick", at = @At("HEAD"), cancellable = true)
    private void preventLodestoneTrackerComponentUpdate(ItemStack stack, ServerLevel world, Entity entity, EquipmentSlot slot, CallbackInfo ci)
    {
        CustomData nbtComponent = stack.get(DataComponents.CUSTOM_DATA);
        if (nbtComponent != null &&
                nbtComponent.nbt != null &&
                nbtComponent.nbt.getBoolean("isDGBonusTracker").orElse(false))
        {
            ci.cancel();
        }
    }
}
