package de.jagenka.mixin;

import de.jagenka.config.Config;
import de.jagenka.gameplay.graplinghook.BlackjackAndHookers;
import de.jagenka.gameplay.traps.TrapsAreNotGay;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
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
            if (TrapsAreNotGay.handleTrapPlacement(context))
            {
                cir.setReturnValue(ActionResult.PASS);
                cir.cancel();
            }
        }

        // Grapple
        if (context.getPlayer() != null && context.getPlayer().getStackInHand(context.getHand()).getItem() == Items.CARROT_ON_A_STICK)
        {
            BlackjackAndHookers.forceTheHooker(context.getWorld(), context.getPlayer());
        }
    }

    @Inject(method = "use", at = @At("HEAD"))
    public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        // Grapple
        if (user.getStackInHand(hand).getItem() == Items.CARROT_ON_A_STICK)
        {
            BlackjackAndHookers.forceTheHooker(world, user);
        }
    }
}
