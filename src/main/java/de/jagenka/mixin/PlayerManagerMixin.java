package de.jagenka.mixin;

import de.jagenka.DGPlayerManager;
import de.jagenka.DGSpawnManager;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin
{
    @Inject(method = "respawnPlayer", at = @At("TAIL"))
    private void respawnPlayer(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir)
    {
        ServerPlayerEntity newPlayer = cir.getReturnValue();
        DGSpawnManager.handleRespawn(newPlayer);
    }
}
