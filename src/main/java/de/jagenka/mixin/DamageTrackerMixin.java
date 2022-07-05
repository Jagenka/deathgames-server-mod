package de.jagenka.mixin;

import de.jagenka.config.Config;
import de.jagenka.stats.StatManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DamageTracker.class)
public class DamageTrackerMixin
{
    @Shadow
    @Final
    private LivingEntity entity;

    @Inject(method = "onDamage", at = @At("TAIL"))
    private void trackPlayerDamage(DamageSource damageSource, float originalHealth, float damage, CallbackInfo ci)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        if (!(this.entity instanceof PlayerEntity damagedPlayer)) return;
        StatManager.addDamageTaken(damagedPlayer.getName().getString(), damage);

        if (!(damageSource.getAttacker() instanceof PlayerEntity attackingPlayer)) return;
        StatManager.addDamageDealt(attackingPlayer.getName().getString(), damage);
    }
}
