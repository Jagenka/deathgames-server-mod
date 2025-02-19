package de.jagenka.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CompassItem.class)
public class CompassItemMixin
{
    @Inject(method = "inventoryTick", at = @At("HEAD"), cancellable = true)
    private void preventLodestoneTrackerComponentUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected, CallbackInfo ci)
    {
        NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent != null &&
                nbtComponent.nbt != null &&
                nbtComponent.nbt.contains("isDGBonusTracker") &&
                nbtComponent.nbt.getBoolean("isDGBonusTracker"))
        {
            ci.cancel();
        }
    }
}
