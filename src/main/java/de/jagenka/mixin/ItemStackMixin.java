package de.jagenka.mixin;

import de.jagenka.config.Config;
import de.jagenka.gameplay.traps.TrapsAreNotGay;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
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

        if (context.getStack().getItem() == Items.BAT_SPAWN_EGG)
        {
            if (TrapsAreNotGay.handleTrapPlacement(context))
            {
                cir.setReturnValue(ActionResult.PASS);
                cir.cancel();
            }
        }

        // other items here
    }
}
