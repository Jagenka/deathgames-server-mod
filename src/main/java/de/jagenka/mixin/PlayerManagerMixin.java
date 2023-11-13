package de.jagenka.mixin;

import de.jagenka.config.Config;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin
{
    @Inject(method = "respawnPlayer", at = @At("TAIL"))
    private void respawnPlayer(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        ServerPlayerEntity newPlayer = cir.getReturnValue();
        de.jagenka.managers.PlayerManager.handleRespawn(newPlayer);
    }

    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        de.jagenka.managers.PlayerManager.onPlayerJoin(player);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void onPlayerLeave(ServerPlayerEntity player, CallbackInfo ci)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        de.jagenka.managers.PlayerManager.onPlayerLeave(player);
    }
}
