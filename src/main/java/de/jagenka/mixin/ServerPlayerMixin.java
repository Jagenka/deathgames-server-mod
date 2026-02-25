package de.jagenka.mixin;

import de.jagenka.config.Config;
import de.jagenka.managers.KillManager;
import de.jagenka.stats.StatManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin
{
    @Inject(method = "die", at = @At("TAIL"))
    private void onDeath(DamageSource damageSource, CallbackInfo ci)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        try
        {
            LivingEntity primeAdversary = ((ServerPlayer) (Object) this).getKillCredit(); // TODO: works?
            if (primeAdversary instanceof ServerPlayer killer)
            {
                KillManager.handlePlayerKill(killer, (ServerPlayer) (Object) this);
                StatManager.handleKillType(damageSource, killer.getName().getString(), ((ServerPlayer) (Object) this).getName().getString());
            }
        } catch (ClassCastException ignored)
        {
        }

        KillManager.handleDeath((ServerPlayer) (Object) this);
        StatManager.handleDeathType(damageSource, ((ServerPlayer) (Object) this).getName().getString());
    }

    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("HEAD"), cancellable = true)
    private void preventDrop(ItemStack itemStack, boolean bl, boolean bl2, CallbackInfoReturnable<ItemEntity> cir)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        cir.setReturnValue(null);
        cir.cancel();
        ((ServerPlayer) (Object) this).inventoryMenu.sendAllDataToRemote();
    }

    @Inject(method = "drop(Z)V", at = @At("HEAD"), cancellable = true)
    private void preventDrop(boolean bl, CallbackInfo ci) {
        if (!Config.INSTANCE.isEnabled()) return;

        ci.cancel();
        ((ServerPlayer) (Object) this).inventoryMenu.sendAllDataToRemote();
    }

    @Inject(method = "awardStat", at = @At("HEAD"))
    private void increaseStat(Stat<?> stat, int amount, CallbackInfo ci)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        if (!((ServerPlayer) (Object) this).isSpectator())
        {
            StatManager.handleStatIncrease(((ServerPlayer) (Object) this).getName().getString(), stat, amount);
        }
    }
}
