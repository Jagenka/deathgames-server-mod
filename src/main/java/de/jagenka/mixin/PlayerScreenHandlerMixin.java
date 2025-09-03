package de.jagenka.mixin;

import de.jagenka.config.Config;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.PlayerScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerScreenHandler.class)
public class PlayerScreenHandlerMixin
{
    @Inject(method = "onContentChanged", at = @At("HEAD"), cancellable = true)
    private void preventOnContentChanged(Inventory inventory, CallbackInfo ci)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        ci.cancel();
    }
}
