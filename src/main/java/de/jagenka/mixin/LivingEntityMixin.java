package de.jagenka.mixin;

import de.jagenka.config.Config;
import de.jagenka.managers.SpawnManager;
import de.jagenka.stats.StatManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin
{
    @Inject(method = "heal", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setHealth(F)V"))
    private void onPlayerHeal(float amount, CallbackInfo ci)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        if ((LivingEntity) (Object) this instanceof PlayerEntity player)
        {
            StatManager.addHealAmount(player.getName().getString(), amount);
        }
    }

    @Inject(method = "tryUseDeathProtector", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;sendEntityStatus(Lnet/minecraft/entity/Entity;B)V"))
    private void onTotemActivated(DamageSource source, CallbackInfoReturnable<Boolean> cir)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        if ((LivingEntity) (Object) this instanceof ServerPlayerEntity player)
        {
            // no longer clearing effects, as death protector component can have specific effects: TODO: move respawn effects to component?
            SpawnManager.INSTANCE.applyRespawnEffects(player);
        }
    }
}
