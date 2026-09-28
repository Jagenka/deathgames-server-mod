package de.jagenka.mixin;

import de.jagenka.config.Config;
import de.jagenka.gameplay.graplinghook.GrapplingHook;
import de.jagenka.gameplay.traps.TrapManager;
import de.jagenka.shop.Shop;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin
{
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    public void useOnBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        // Traps
        if (context.getItemInHand().getItem() == Items.BAT_SPAWN_EGG)
        {
            if (TrapManager.handleTrapPlacement(context))
            {
                cir.setReturnValue(InteractionResult.PASS);
                cir.cancel();
            }
        }

        // ender pearls in shop
        if (context.getItemInHand().getItem() == Items.ENDER_PEARL && Shop.INSTANCE.isInShopBounds(context.getPlayer()))
        {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        // Grapple
        if (player instanceof ServerPlayer serverPlayer && serverPlayer.getItemInHand(hand).getItem() == GrapplingHook.Companion.getItemItem())
        {
            GrapplingHook.Companion.getDefault().forceTheHooker(serverPlayer, serverPlayer.getItemInHand(hand));
        }

        // ender pearls in shop
        ItemStack stackInHand = player.getItemInHand(hand);

        if (stackInHand.getItem() == Items.ENDER_PEARL && Shop.INSTANCE.isInShopBounds(player))
        {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
            player.inventoryMenu.sendAllDataToRemote();
        }
    }
}
