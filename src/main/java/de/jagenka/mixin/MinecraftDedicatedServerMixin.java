package de.jagenka.mixin;

import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftDedicatedServer.class)
public class MinecraftDedicatedServerMixin
{
    @Inject(method = "getSpawnProtectionRadius", at = @At("HEAD"), cancellable = true)
    private void spawnProtectionIsZero(CallbackInfoReturnable<Integer> cir)
    {
        cir.setReturnValue(0);
        cir.cancel();
    }
}
