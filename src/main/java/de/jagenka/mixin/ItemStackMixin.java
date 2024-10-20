package de.jagenka.mixin;

import de.jagenka.config.Config;
import de.jagenka.gameplay.graplinghook.BlackjackAndHookers;
import de.jagenka.gameplay.traps.TrapManager;
import de.jagenka.shop.Shop;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin
{
    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    public void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        // Traps
        if (context.getStack().getItem() == Items.BAT_SPAWN_EGG)
        {
            if (TrapManager.handleTrapPlacement(context))
            {
                cir.setReturnValue(ActionResult.PASS);
                cir.cancel();
            }
        }

        // Grapple
        if (context.getPlayer() != null && context.getPlayer().getStackInHand(context.getHand()).getItem() == BlackjackAndHookers.getItemItem())
        {
            if (context.getPlayer() instanceof ServerPlayerEntity player)
            {
                BlackjackAndHookers.forceTheHooker(context.getWorld(), player, context.getPlayer().getStackInHand(context.getHand()));
            }
        }

        // ender pearls in shop
        if (context.getStack().getItem() == Items.ENDER_PEARL && Shop.INSTANCE.isInShopBounds(context.getPlayer()))
        {
            cir.setReturnValue(ActionResult.FAIL);
            cir.cancel();
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        ItemStack stackInHand = user.getStackInHand(hand);

        // Grapple
        if (stackInHand.getItem() == BlackjackAndHookers.getItemItem())
        {
            if (user instanceof ServerPlayerEntity player)
            {
                BlackjackAndHookers.forceTheHooker(world, player, stackInHand);
            }
        }

        // ender pearls in shop
        if (stackInHand.getItem() == Items.ENDER_PEARL && Shop.INSTANCE.isInShopBounds(user))
        {
            if (user instanceof ServerPlayerEntity)
            {
                ((ServerPlayerEntity) user).networkHandler.sendPacket(
                        new ScreenHandlerSlotUpdateS2CPacket(-2, 0, user.getInventory().selectedSlot, user.getInventory().getStack(user.getInventory().selectedSlot)));
            }
            cir.setReturnValue(TypedActionResult.fail(stackInHand));
            cir.cancel();
        }
    }
}
