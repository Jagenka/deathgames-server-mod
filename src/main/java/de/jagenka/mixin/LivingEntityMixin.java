package de.jagenka.mixin;

import de.jagenka.config.Config;
import de.jagenka.stats.StatManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin
{
    @Inject(method = "heal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"))
    private void onPlayerHeal(float amount, CallbackInfo ci)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        if ((LivingEntity) (Object) this instanceof Player player)
        {
            StatManager.addHealAmount(player.getName().getString(), amount);
        }
    }

    // no longer clearing or giving effects on death, as death protector component can have specific effects: respawn effects now need to be set in shop config
}
