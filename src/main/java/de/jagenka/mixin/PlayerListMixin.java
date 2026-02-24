package de.jagenka.mixin;

import de.jagenka.config.Config;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class PlayerListMixin
{
    @Inject(method = "respawn", at = @At("TAIL"))
    private void respawnPlayer(ServerPlayer player, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayer> cir)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        ServerPlayer newPlayer = cir.getReturnValue();
        de.jagenka.managers.PlayerManager.handleRespawn(newPlayer);
    }

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void onPlayerConnect(Connection connection, ServerPlayer player, CommonListenerCookie clientData, CallbackInfo ci)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        de.jagenka.managers.PlayerManager.onPlayerJoin(player);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void onPlayerLeave(ServerPlayer player, CallbackInfo ci)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        de.jagenka.managers.PlayerManager.onPlayerLeave(player);
    }
}
