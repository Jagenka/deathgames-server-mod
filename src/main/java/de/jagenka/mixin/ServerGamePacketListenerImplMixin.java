package de.jagenka.mixin;

import de.jagenka.config.Config;
import de.jagenka.shop.Shop;
import de.jagenka.team.TeamSelectorUI;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin
{
    @Shadow
    public ServerPlayer player;

    @Inject(method = "onPlayerAction", at = @At("HEAD"), cancellable = true)
    private void openShop(ServerboundPlayerActionPacket packet, CallbackInfo ci)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        if (packet.getAction().equals(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND))
        {
            if (Shop.showInterfaceIfInShop(this.player)) ci.cancel();
            else if (TeamSelectorUI.showInterfaceIfInLobby(this.player)) ci.cancel();
        }
    }
}
