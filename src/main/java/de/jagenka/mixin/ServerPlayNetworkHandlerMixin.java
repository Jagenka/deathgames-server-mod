package de.jagenka.mixin;

import de.jagenka.shop.Shop;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin
{
    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onPlayerAction", at = @At("HEAD"), cancellable = true)
    private void openShop(PlayerActionC2SPacket packet, CallbackInfo ci)
    {
        if (packet.getAction().equals(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND))
        {
            if (Shop.showInterfaceIfInShop(this.player)) ci.cancel();
        }
    }
}
