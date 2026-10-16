package de.jagenka.mixin;

import de.jagenka.GameState;
import de.jagenka.stats.StatManager;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CombatTracker.class)
public class CombatTrackerMixin
{
    @Shadow
    @Final
    private LivingEntity mob;

    @Inject(method = "recordDamage", at = @At("TAIL"))
    private void trackPlayerDamage(DamageSource source, float damage, CallbackInfo ci)
    {
        if (GameState.Companion.getModDisabled()) return;

        if (!(this.mob instanceof Player damagedPlayer)) return;
        StatManager.addDamageTaken(damagedPlayer.getName().getString(), damage);

        if (!(source.getEntity() instanceof Player attackingPlayer))
            return;
        StatManager.addDamageDealt(attackingPlayer.getName().getString(), damage);
    }
}
