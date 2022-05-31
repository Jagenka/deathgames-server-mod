package de.jagenka.mixin;

import de.jagenka.DGKillManager;
import de.jagenka.Testing;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin
{
    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource damageSource, CallbackInfo ci)
    {
        Entity attacker = damageSource.getAttacker();
        DGKillManager.handleDeath(attacker, (ServerPlayerEntity) (Object) this);
    }

    // I use this for testing
    @Inject(method = "dropSelectedItem", at = @At("HEAD"))
    private void dropItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir)
    {
        Testing.dropTest();
        //TODO: prevent drop
    }
}
