package de.jagenka.mixin;

import de.jagenka.config.Config;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin
{
    @Shadow public abstract boolean giveItemStack(ItemStack stack);

    @Shadow @Final public PlayerScreenHandler playerScreenHandler;

    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/ItemEntity;", at = @At("HEAD"))
    private void preventDrop(ItemStack stack, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        this.giveItemStack(stack);
        this.playerScreenHandler.updateToClient();
    }
}
