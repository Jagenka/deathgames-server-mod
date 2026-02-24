package de.jagenka.mixin;

import net.minecraft.server.ServerInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerInterface.class)
public class MinecraftServerInterfaceMixin
{
    @Inject(method = "spawnProtectionRadius", at = @At("HEAD"), cancellable = true)
    private void spawnProtectionIsZero(CallbackInfoReturnable<Integer> cir)
    {
        cir.setReturnValue(0);
        cir.cancel();
    }
}
