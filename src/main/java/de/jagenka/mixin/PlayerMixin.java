package de.jagenka.mixin;

import de.jagenka.config.Config;
import de.jagenka.gameplay.graplinghook.BlackjackAndHookers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin
{
    @Shadow
    public abstract boolean addItem(ItemStack stack);

    @Shadow
    @Final
    public InventoryMenu inventoryMenu;

    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("HEAD"))
    private void preventDrop(ItemStack stack, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        this.addItem(stack);
        this.inventoryMenu.sendAllDataToRemote();
    }

    @Inject(method = "wantsToStopRiding", at = @At("HEAD"), cancellable = true)
    private void preventDismount(CallbackInfoReturnable<Boolean> cir)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        if ((Player) (Object) this instanceof ServerPlayer player)
        {
            if (BlackjackAndHookers.INSTANCE.isRidingHook(player))
            {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }
}
