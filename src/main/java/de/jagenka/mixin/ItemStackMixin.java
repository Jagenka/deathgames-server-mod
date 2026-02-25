package de.jagenka.mixin;

import de.jagenka.config.Config;
import de.jagenka.gameplay.graplinghook.BlackjackAndHookers;
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

        // Grapple
        if (context.getPlayer() != null && context.getPlayer().getStackInHand(context.getHand()).getItem() == BlackjackAndHookers.getItemItem())
        {
            if (context.getPlayer() instanceof ServerPlayer player)
            {
                BlackjackAndHookers.forceTheHooker(context.getWorld(), player, context.getPlayer().getStackInHand(context.getHand()));
            }
        }

        // ender pearls in shop
        if (context.getStack().getItem() == Items.ENDER_PEARL && Shop.INSTANCE.isInShopBounds(context.getPlayer()))
        {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        ItemStack stackInHand = user.getStackInHand(hand);

        // Grapple
        if (stackInHand.getItem() == BlackjackAndHookers.getItemItem())
        {
            if (user instanceof ServerPlayer player)
            {
                BlackjackAndHookers.forceTheHooker(world, player, stackInHand);
            }
        }

        // ender pearls in shop
        if (stackInHand.getItem() == Items.ENDER_PEARL && Shop.INSTANCE.isInShopBounds(user))
        {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
            user.currentScreenHandler.sendContentUpdates();
        }
    }
}
