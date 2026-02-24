package de.jagenka.mixin;

import de.jagenka.config.Config;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
public class InventoryMenuMixin
{
    @Inject(method = "onContentChanged", at = @At("HEAD"), cancellable = true)
    private void preventOnContentChanged(Container inventory, CallbackInfo ci)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        ci.cancel();
    }
}
