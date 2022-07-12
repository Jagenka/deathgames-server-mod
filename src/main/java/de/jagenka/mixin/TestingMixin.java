package de.jagenka.mixin;

import de.jagenka.config.Config;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class TestingMixin
{
    @Shadow public abstract Text getName();

    @Inject(method = "useOnBlock", at = @At("HEAD"))
    private void onUseOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir)
    {
        if (Config.INSTANCE.isEnabled()) return;

        if (this.getName().getString().equals(Items.STICK.getName().getString()))
        {

        }
    }
}
