package de.jagenka.mixin;

import de.jagenka.config.Config;
import de.jagenka.stats.StatManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    // no longer clearing or giving effects on death, as death protector component can have specific effects: respawn effects now need to be set in shop config
}
