package de.jagenka.mixin;

import de.jagenka.Testing;
import de.jagenka.managers.KillManager;
import de.jagenka.stats.StatManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin
{
    @Inject(method = "onDeath", at = @At("TAIL"))
    private void onDeath(DamageSource damageSource, CallbackInfo ci)
    {
        try
        {
            LivingEntity primeAdversary = ((ServerPlayerEntity) (Object) this).getPrimeAdversary();
            if (primeAdversary instanceof ServerPlayerEntity killer)
            {
                KillManager.handlePlayerKill(killer, (ServerPlayerEntity) (Object) this);
                StatManager.handleKillType(damageSource, killer.getName().getString(), ((ServerPlayerEntity) (Object) this).getName().getString());
            }
        } catch (ClassCastException ignored)
        {
        }

        KillManager.handleDeath((ServerPlayerEntity) (Object) this);
        StatManager.handleDeathType(damageSource, ((ServerPlayerEntity) (Object) this).getName().getString());
    }

    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    private void dropItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir)
    {
        cir.setReturnValue(false);
        cir.cancel();
        ((ServerPlayerEntity) (Object) this).playerScreenHandler.updateToClient();
    }

    @Inject(method = "increaseStat", at = @At("HEAD"))
    private void increaseStat(Stat<?> stat, int amount, CallbackInfo ci)
    {
        if (!((ServerPlayerEntity) (Object) this).isSpectator())
        {
            StatManager.handleStatIncrease(((ServerPlayerEntity) (Object) this).getName().getString(), stat, amount);
        }
    }
}
